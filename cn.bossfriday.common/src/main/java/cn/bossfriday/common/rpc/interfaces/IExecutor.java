package cn.bossfriday.common.rpc.interfaces;

import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.transport.RpcMessage;

/**
 * IExecutor
 *
 * @author chenx
 */
public interface IExecutor {

    /**
     * process
     *
     * @param message
     * @param actorSystem
     */
    void process(RpcMessage message, ActorSystem actorSystem);

    /**
     * destroy
     */
    void destroy();
}
