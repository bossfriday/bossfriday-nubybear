package cn.bossfriday.common.rpc.actor.pool;

import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.lang.reflect.Constructor;

/**
 * ActorFactory
 *
 * @author chenx
 */
public class ActorFactory extends BasePooledObjectFactory<BaseUntypedActor> {

    private Class<? extends BaseUntypedActor> cls;
    private Object[] args;

    public ActorFactory(Class<? extends BaseUntypedActor> cls, Object[] args) {
        this.cls = cls;
        this.args = args;
    }

    @Override
    public BaseUntypedActor create() throws Exception {
        if (this.cls != null) {
            BaseUntypedActor baseUntypedActor = null;
            if (this.args != null && this.args.length > 0) {
                Class<?>[] clsArray = new Class<?>[this.args.length];
                for (int i = 0; i < this.args.length; i++) {
                    clsArray[i] = this.args[i].getClass();
                }
                Constructor<? extends BaseUntypedActor> constructor = this.cls.getConstructor(clsArray);
                baseUntypedActor = constructor.newInstance(this.args);
            } else {
                baseUntypedActor = this.cls.newInstance();
            }

            return baseUntypedActor;
        }

        return null;
    }

    @Override
    public PooledObject<BaseUntypedActor> wrap(BaseUntypedActor obj) {
        return new DefaultPooledObject<>(obj);
    }
}