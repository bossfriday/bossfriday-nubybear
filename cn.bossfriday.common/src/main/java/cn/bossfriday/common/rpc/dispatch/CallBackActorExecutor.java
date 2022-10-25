package cn.bossfriday.common.rpc.dispatch;

import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.rpc.interfaces.IExecutor;
import cn.bossfriday.common.rpc.transport.RpcMessage;

import java.util.concurrent.ExecutorService;

/**
 * CallBackActorExecutor
 *
 * @author chenx
 */
public class CallBackActorExecutor implements IExecutor {

    private ExecutorService callBackThreadPool;
    private BaseUntypedActor actor = null;
    private long timeoutTimestamp = 0;

    public CallBackActorExecutor(BaseUntypedActor actor, long ttl, ExecutorService callBackThreadPool) {
        this.actor = actor;
        this.timeoutTimestamp = System.currentTimeMillis() + ttl;
        this.callBackThreadPool = callBackThreadPool;
    }

    /**
     * get TTL ms
     */
    public long ttl() {
        return this.timeoutTimestamp - System.currentTimeMillis();
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
            this.callBackThreadPool.submit(() -> {
                try {
                    this.actor.onReceive(message, actorSystem);
                } catch (Exception e) {
                    this.actor.onFailed(e);
                }
            });
        }
    }

    @Override
    public void destroy() {
        // just  reserved
    }
}
