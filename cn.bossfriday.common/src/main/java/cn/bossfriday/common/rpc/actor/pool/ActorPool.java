package cn.bossfriday.common.rpc.actor.pool;

import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * ActorPool
 *
 * @author chenx
 */
@Slf4j
public class ActorPool {

    private GenericObjectPool<BaseUntypedActor> pool = null;
    private ActorFactory actorFactory = null;

    @Getter
    private String method;

    public ActorPool(int min, int max, String method, Class<? extends BaseUntypedActor> cls, Object... args) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig<>();
        config.setMinIdle(min);
        config.setMaxIdle(max);
        config.setMaxTotal(max);
        config.setLifo(false);
        config.setBlockWhenExhausted(true);
        config.setJmxNamePrefix("actor_" + method);

        this.method = method;
        this.actorFactory = new ActorFactory(cls, args);
        this.pool = new GenericObjectPool<>(this.actorFactory, config);
    }

    /**
     * borrowObject
     */
    public BaseUntypedActor borrowObject() {
        try {
            return this.pool.borrowObject();
        } catch (Exception e) {
            log.error("ActorPool.borrowObject() error!", e);
        }

        return null;
    }

    /**
     * returnObject
     */
    public void returnObject(BaseUntypedActor obj) {
        this.pool.returnObject(obj);
    }

    /**
     * close
     */
    public void close() {
        this.pool.close();
    }

    /**
     * clear
     */
    public void clear() {
        this.pool.clear();
    }
}
