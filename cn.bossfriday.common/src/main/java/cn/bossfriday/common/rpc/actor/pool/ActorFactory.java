package cn.bossfriday.common.rpc.actor.pool;

import cn.bossfriday.common.rpc.actor.UntypedActor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.lang.reflect.Constructor;

public class ActorFactory extends BasePooledObjectFactory<UntypedActor> {
    private Class<? extends UntypedActor> cls;
    private Object[] args;

    public ActorFactory(Class<? extends UntypedActor> cls, Object[] args) {
        this.cls = cls;
        this.args = args;
    }

    @Override
    public UntypedActor create() throws Exception {
        if (this.cls != null) {
            UntypedActor untypedActor = null;
            if (args != null && args.length > 0) {
                Class<?>[] clsArray = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    clsArray[i] = args[i].getClass();
                }
                Constructor<? extends UntypedActor> constructor = this.cls.getConstructor(clsArray);
                untypedActor = constructor.newInstance(args);
            } else {
                untypedActor = cls.newInstance();
            }

            return untypedActor;
        }

        return null;
    }

    @Override
    public PooledObject<UntypedActor> wrap(UntypedActor obj) {
        return new DefaultPooledObject<UntypedActor>(obj);
    }
}