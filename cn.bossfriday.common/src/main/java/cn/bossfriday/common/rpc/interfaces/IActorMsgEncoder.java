package cn.bossfriday.common.rpc.interfaces;

public interface IActorMsgEncoder {
    /**
     * encode
     *
     * @param obj
     * @return
     */
    byte[] encode(Object obj);
}
