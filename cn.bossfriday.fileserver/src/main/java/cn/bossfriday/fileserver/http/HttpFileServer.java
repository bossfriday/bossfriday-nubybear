package cn.bossfriday.fileserver.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpFileServer {
    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.SO_REUSEADDR, true);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_SNDBUF, 1024*1024*10);
            b.option(ChannelOption.SO_RCVBUF, 1024*1024*10);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                               @Override
                               protected void initChannel(SocketChannel socketChannel) {
                                   socketChannel.pipeline().addLast(new HttpRequestDecoder());
                                   socketChannel.pipeline().addLast(new HttpResponseEncoder());
                                   socketChannel.pipeline().addLast(new HttpContentCompressor());
                                   socketChannel.pipeline().addLast(new HttpFileServerHandler());
                               }
                           }
            );

            Channel ch = b.bind(port).sync().channel();

            log.info("HttpFileServer start() done, port:" + port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpFileServer().start(18080);
    }
}
