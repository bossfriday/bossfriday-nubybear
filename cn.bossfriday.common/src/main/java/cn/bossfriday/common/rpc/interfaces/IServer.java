package cn.bossfriday.common.rpc.interfaces;

import io.netty.channel.EventLoopGroup;

public interface IServer {
    /**
     * run
     *
     * @param bossGroup
     * @param workerGroup
     * @throws Exception
     */
    void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception;

    /**
     * close
     *
     * @throws Exception
     */
    void close() throws Exception;
}
