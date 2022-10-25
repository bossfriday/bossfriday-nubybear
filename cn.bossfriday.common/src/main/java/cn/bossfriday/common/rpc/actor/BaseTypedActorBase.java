package cn.bossfriday.common.rpc.actor;

/**
 * BaseTypedActor
 *
 * @author chenx
 */
public abstract class BaseTypedActorBase<T> extends BaseUntypedActor {

    /**
     * onMessageReceived
     *
     * @param msg
     */
    public abstract void onMessageReceived(T msg);

    @Override
    public void onReceive(Object msg) {
        this.onMessageReceived((T) msg);
    }
}
