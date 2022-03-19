package cn.bossfriday.common.rpc.actor.pool;

import cn.bossfriday.common.rpc.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Slf4j
public class ActorPool {
    private GenericObjectPool<UntypedActor> pool = null;
    private ActorFactory actorFactory = null;
    private String method;

    public ActorPool(int min, int max, String method, Class<? extends UntypedActor> cls, Object... args) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(min);
        config.setMaxIdle(max);
        config.setMaxTotal(max);
        config.setLifo(false);
        config.setBlockWhenExhausted(true);
        config.setJmxNamePrefix("actor_" + method);

        this.method = method;
        this.actorFactory = new ActorFactory(cls, args);
        this.pool = new GenericObjectPool<>(actorFactory, config);
    }

    /**
     * borrowObject
     */
    public UntypedActor borrowObject() {
        try {
            return this.pool.borrowObject();
        } catch (Exception e) {
            log.error("ActorPool.getObject() error!", e);
        }

        return null;
    }

    /**
     * returnObject
     */
    public void returnObject(UntypedActor obj) {
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
