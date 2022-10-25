package cn.bossfriday.common.rpc.interfaces;

import cn.bossfriday.common.rpc.transport.RpcMessage;

/**
 * IMsgHandler
 *
 * @author chenx
 */
public interface IMsgHandler {
    /**
     * msgHandle
     *
     * @param msg
     */
    void msgHandle(RpcMessage msg);
}
