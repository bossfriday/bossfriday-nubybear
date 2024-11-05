package cn.bossfriday.fileserver.http;

import cn.bossfriday.im.common.conf.SystemConfigLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpFileServer
 *
 * @author chenx
 */
@Slf4j
@SuppressWarnings("squid:S6548")
public class HttpFileServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    private HttpFileServer() {
        // just do nothing
    }

    /**
     * getInstance
     */
    public static HttpFileServer getInstance() {
        return HttpFileServer.SingletonHolder.INSTANCE;
    }

    /**
     * start
     */
    public void start() throws InterruptedException {
        int port = SystemConfigLoader.getInstance().getFileServerConfig().getHttpPort();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.handler(new LoggingHandler(LogLevel.WARN));
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.option(ChannelOption.SO_REUSEADDR, true);
        b.option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 10);
        b.childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           protected void initChannel(SocketChannel socketChannel) {
                               socketChannel.pipeline().addLast(new HttpRequestDecoder());
                               socketChannel.pipeline().addLast(new HttpResponseEncoder());
                               socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                               socketChannel.pipeline().addLast(new HttpFileServerHandler());
                           }
                       }
        );

        this.serverChannel = b.bind(port).sync().channel();
        log.info("HttpFileServer start done, port: {}", port);
    }

    /**
     * stop
     */
    public void stop() throws InterruptedException {
        if (this.serverChannel != null) {
            this.serverChannel.close();
            this.serverChannel.closeFuture().sync();
        }

        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
        }

        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
    }

    /**
     * SingletonHolder
     */
    private static class SingletonHolder {
        private static final HttpFileServer INSTANCE = new HttpFileServer();
    }
}
