package cn.bossfriday.common.rpc.interfaces;

/**
 * IActorMsgEncoder
 *
 * @author chenx
 */
public interface IActorMsgEncoder {

    /**
     * encode
     *
     * @param obj
     * @return
     */
    byte[] encode(Object obj);
}
