package cn.bossfriday.common.rpc.interfaces;

import io.netty.channel.EventLoopGroup;

/**
 * IServer
 *
 * @author chenx
 */
public interface IServer {

    /**
     * start
     *
     * @param bossGroup
     * @param workerGroup
     */
    void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup);

    /**
     * stop
     */
    void stop();
}
