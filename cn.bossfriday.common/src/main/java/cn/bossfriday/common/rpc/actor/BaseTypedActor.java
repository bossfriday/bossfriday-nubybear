package cn.bossfriday.common.rpc.actor;

/**
 * BaseTypedActor
 *
 * @author chenx
 */
public abstract class BaseTypedActor<T> extends BaseUntypedActor {

    /**
     * onMessageReceived
     *
     * @param msg
     */
    public abstract void onMessageReceived(T msg);

    @Override
    public void onMsgReceive(Object msg) {
        this.onMessageReceived((T) msg);
    }
}
