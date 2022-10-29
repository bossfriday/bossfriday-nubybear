package cn.bossfriday.common.rpc.interfaces;

/**
 * IActorMsgDecoder
 *
 * @author chenx
 */
public interface IActorMsgDecoder {

    /**
     * decode
     *
     * @param bytes
     * @return
     */
    Object decode(byte[] bytes);
}
