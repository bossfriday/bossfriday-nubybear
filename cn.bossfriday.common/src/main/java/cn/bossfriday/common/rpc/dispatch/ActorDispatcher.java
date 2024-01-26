package cn.bossfriday.common.rpc.dispatch;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.rpc.interfaces.IExecutor;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import cn.bossfriday.common.utils.ThreadPoolUtil;
import cn.bossfriday.common.utils.UUIDUtil;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ActorDispatcher
 *
 * @author chenx
 */
@Slf4j
public class ActorDispatcher {

    public static final ExecutorService DEFAULT_THREAD_POOL = ThreadPoolUtil.getThreadPool(Const.THREAD_POOL_NAME_ACTORS_POOLS, ThreadPoolUtil.AVAILABLE_PROCESSORS * 2);

    private static final ExecutorService DISPATCH_THREAD_POOL = ThreadPoolUtil.getThreadPool(Const.THREAD_POOL_NAME_ACTORS_DISPATCH, 2);
    private static final ExecutorService CALL_BACK_THREAD_POOL = ThreadPoolUtil.getThreadPool(Const.THREAD_POOL_NAME_ACTORS_CALLBACK, ThreadPoolUtil.AVAILABLE_PROCESSORS);

    private ConcurrentHashMap<String, IExecutor> actorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IExecutor> callbackActorMap = new ConcurrentHashMap<>();
    private ActorSystem actorSystem;

    public ActorDispatcher(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    /**
     * registerActor
     *
     * @param method
     * @param min
     * @param max
     * @param cls
     * @param args
     */
    public void registerActor(String method,
                              int min,
                              int max, Class<? extends BaseUntypedActor> cls,
                              Object... args) {
        ActorExecutor executor = new ActorExecutor(min, max, method, cls, args);
        this.actorMap.putIfAbsent(method, executor);
    }

    /**
     * registerActor
     *
     * @param method
     * @param min
     * @param max
     * @param pool
     * @param cls
     * @param args
     */
    public void registerActor(String method,
                              int min,
                              int max,
                              ExecutorService pool,
                              Class<? extends BaseUntypedActor> cls,
                              Object... args) {
        ActorExecutor executor = new ActorExecutor(min, max, method, pool, cls, args);
        this.actorMap.putIfAbsent(method, executor);
    }

    /**
     * registerCallBackActor
     *
     * @param key
     * @param actor
     * @param ttl
     */
    public void registerCallBackActor(byte[] key, BaseUntypedActor actor, long ttl) {
        String standKey = UUIDUtil.getShortString(key);
        this.callbackActorMap.putIfAbsent(standKey, new CallBackActorExecutor(actor, ttl, CALL_BACK_THREAD_POOL));
        HashWheelTimer.putTimeOutTask(new CallBackActorTimerTask(standKey), ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * dispatch
     *
     * @param message
     */
    public void dispatch(RpcMessage message) {
        if (message == null) {
            return;
        }

        DISPATCH_THREAD_POOL.submit(() -> {
            try {
                IExecutor executor = null;
                if (message.getTargetMethod() == null) {
                    // callback actor
                    byte[] key = message.getSession();
                    executor = this.callbackActorMap.remove(UUIDUtil.getShortString(key));
                } else {
                    // pool actor
                    String method = message.getTargetMethod();
                    executor = this.actorMap.get(method);
                }

                if (executor == null) {
                    throw new ServiceRuntimeException("executor is null!");
                }

                executor.process(message, this.actorSystem);
            } catch (Exception e) {
                log.error("ActorDispatcher error!", e);
            }
        });
    }

    /**
     * stop
     */
    public void stop() {
        for (Map.Entry<String, IExecutor> entry : this.actorMap.entrySet()) {
            entry.getValue().destroy();
        }

        // 不使用hashMap.clear()防止hashMap rehash不缩容导致的OOM
        this.actorMap = new ConcurrentHashMap<>(16);
        this.callbackActorMap = new ConcurrentHashMap<>(16);
    }

    /**
     * CallBackActorTimerTask
     */
    private class CallBackActorTimerTask implements TimerTask {
        private String actorKey;

        public CallBackActorTimerTask(String actorKey) {
            this.actorKey = actorKey;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            IExecutor executor = ActorDispatcher.this.callbackActorMap.get(this.actorKey);
            if (executor instanceof CallBackActorExecutor) {
                CallBackActorExecutor callbackActor = (CallBackActorExecutor) executor;
                long diff = callbackActor.ttl();
                if (diff <= 0) {
                    ActorDispatcher.this.callbackActorMap.remove(this.actorKey);
                    callbackActor.onTimeout(this.actorKey);
                } else {
                    HashWheelTimer.putTimeOutTask(new CallBackActorTimerTask(this.actorKey), diff, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
