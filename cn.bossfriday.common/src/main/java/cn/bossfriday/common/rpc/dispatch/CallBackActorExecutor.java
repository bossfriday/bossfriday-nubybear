package cn.bossfriday.common.rpc.dispatch;

import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.common.rpc.interfaces.IExecutor;
import cn.bossfriday.common.rpc.transport.RpcMessage;

import java.util.concurrent.ExecutorService;

public class CallBackActorExecutor implements IExecutor {
    private ExecutorService callBackThreadPool;
    private UntypedActor actor = null;
    private long timeoutTimestamp = 0;

    public CallBackActorExecutor(UntypedActor actor, long ttl, ExecutorService callBackThreadPool) {
        this.actor = actor;
        this.timeoutTimestamp = System.currentTimeMillis() + ttl;
        this.callBackThreadPool = callBackThreadPool;
    }

    /**
     * get TTL ms
     */
    public long ttl() {
        return timeoutTimestamp - System.currentTimeMillis();
    }

    /**
     * onTimeout
     */
    public void onTimeout(String actorKey) {
        this.actor.onTimeout(actorKey);
    }

    @Override
    public void process(RpcMessage message, ActorSystem actorSystem) {
        if (this.actor != null) {
            callBackThreadPool.submit(() -> {
                try {
                    actor.onReceive(message, actorSystem);
                } catch (Exception e) {
                    actor.onFailed(e);
                }
            });
        }
    }

    @Override
    public void destroy() {

    }
}
