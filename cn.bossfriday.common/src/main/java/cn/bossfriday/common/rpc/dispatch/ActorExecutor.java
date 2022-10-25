package cn.bossfriday.common.rpc.dispatch;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.rpc.actor.pool.ActorPool;
import cn.bossfriday.common.rpc.interfaces.IExecutor;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ExecutorService;

/**
 * ActorExecutor
 *
 * @author chenx
 */
@Slf4j
public class ActorExecutor implements IExecutor {

    private ExecutorService processThreadPool = null;
    private Class<? extends BaseUntypedActor> actorCls = null;
    private int min;
    private int max;
    private String method;
    private ActorPool actorPool;

    public ActorExecutor(int min, int max, String method, Class<? extends BaseUntypedActor> cls, Object... args) {
        this(min, max, method, ActorDispatcher.DEFAULT_THREAD_POOL, cls, args);
    }

    public ActorExecutor(int min,
                         int max,
                         String method,
                         ExecutorService threadPool,
                         Class<? extends BaseUntypedActor> cls,
                         Object... args) {
        if (StringUtils.isEmpty(method)) {
            throw new BizException("ActorExecutor.method is null or empty!");
        }

        this.processThreadPool = threadPool;
        this.actorCls = cls;
        this.min = min;
        this.max = max;
        this.method = method;
        this.actorPool = new ActorPool(this.min, this.max, this.method, this.actorCls, args);
    }

    @Override
    public void process(RpcMessage message, ActorSystem actorSystem) {
        this.processThreadPool.execute(() -> {
            BaseUntypedActor actor = null;
            try {
                actor = this.actorPool.borrowObject();
                if (actor != null) {
                    actor.onReceive(message, actorSystem);
                }
            } catch (Exception e) {
                log.error("ActorExecutor.process() error!", e);
            } finally {
                if (actor != null) {
                    actor.clean();
                    this.actorPool.returnObject(actor);
                }
            }
        });
    }

    @Override
    public void destroy() {
        if (this.actorPool != null) {
            this.actorPool.close();
        }

        if (this.processThreadPool != null
                && this.processThreadPool != ActorDispatcher.DEFAULT_THREAD_POOL
                && !this.processThreadPool.isShutdown()) {
            this.processThreadPool.shutdown();
        }
    }
}
