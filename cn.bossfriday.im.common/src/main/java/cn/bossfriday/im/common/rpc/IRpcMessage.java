package cn.bossfriday.im.common.rpc;

import cn.bossfriday.common.router.RoutableBean;

/**
 * IRpcMessage
 *
 * @author chenx
 */
public interface IRpcMessage {

    /**
     * getRoutableBean
     */
    RoutableBean<Object> getRoutableBean();

    /**
     * getActorContext
     */
    ActorContext getActorContext();
}
