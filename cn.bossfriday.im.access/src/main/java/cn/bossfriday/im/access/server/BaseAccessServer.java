package cn.bossfriday.im.access.server;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * AccessServer
 * <p>
 * 考虑将来可能要支持WebSocket，因此定义一个抽象类；
 *
 * @author chenx
 */
@Slf4j
public abstract class BaseAccessServer {

    protected EventLoopGroup bossGroup = new NioEventLoopGroup();
    protected EventLoopGroup workerGroup = new NioEventLoopGroup();

    protected final int port;
    protected Channel channel;
    protected IMqttListener listener;

    protected BaseAccessServer(int port, IMqttListener listener) {
        this.port = port;
        this.listener = listener;
    }

    /**
     * start
     */
    public abstract void start() throws InterruptedException;

    /**
     * stop
     */
    public void stop() throws InterruptedException {
        if (this.channel != null) {
            this.channel.close();
            this.channel.closeFuture().sync();
        }

        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
        }

        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
    }
}
