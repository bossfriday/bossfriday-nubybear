package cn.bossfriday.common.rpc.actor;

public abstract class TypedActor<T> extends UntypedActor {
    /**
     * onMessageReceived
     */
    public abstract void onMessageReceived(T msg) throws Exception;

    @Override
    public void onReceive(Object msg) throws Exception {
        try {
            onMessageReceived((T) msg);
        } finally {
            msg = null;
        }
    }
}
