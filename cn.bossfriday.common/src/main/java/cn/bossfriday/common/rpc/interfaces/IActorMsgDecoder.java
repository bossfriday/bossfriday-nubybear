package cn.bossfriday.common.rpc.interfaces;

public interface IActorMsgDecoder {
    /**
     * decode
     *
     * @param bytes
     * @return
     */
    Object decode(byte[] bytes);
}
