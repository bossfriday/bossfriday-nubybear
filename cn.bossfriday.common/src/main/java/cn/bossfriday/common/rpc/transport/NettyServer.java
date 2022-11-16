package cn.bossfriday.common.rpc.transport;

import cn.bossfriday.common.rpc.interfaces.IMsgHandler;
import cn.bossfriday.common.rpc.interfaces.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * NettyServer
 *
 * @author chenx
 */
@Slf4j
public class NettyServer implements IServer {

    private int port;
    private Channel channel = null;
    private final IMsgHandler msgHandler;

    public NettyServer(int port, IMsgHandler msgHandler) {
        this.port = port;
        this.msgHandler = msgHandler;
    }

    @Override
    public void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new NettyServerHandler(NettyServer.this.msgHandler));
                        }

                    });
            this.channel = b.bind(this.port).sync().channel();
        } catch (InterruptedException interEx) {
            log.error("NettyServer.start() InterruptedException!", interEx);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("NettyServer.start() error!", ex);
        }
    }

    @Override
    public void stop() {
        try {
            if (this.channel != null) {
                this.channel.close();
                this.channel.closeFuture().sync();
            }
        } catch (InterruptedException interEx) {
            log.error("NettyServer.stop() InterruptedException!", interEx);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("NettyServer.stop() error!", ex);
        }
    }
}
