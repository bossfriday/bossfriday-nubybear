package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.utils.ThreadPoolUtil;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
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

import java.util.concurrent.ExecutorService;

/**
 * HttpFileServer
 *
 * @author chenx
 */
@Slf4j
public class HttpFileServer {

    private static ExecutorService executor = ThreadPoolUtil.getSingleThreadExecutor("HttpFileServer");
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static Channel serverChannel;

    private HttpFileServer() {
        // do nothing
    }

    /**
     * start
     */
    public static void start() {
        executor.execute(() -> {
            int port = FileServerConfigManager.getFileServerConfig().getHttpPort();
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup);
                b.channel(NioServerSocketChannel.class);
                b.handler(new LoggingHandler(LogLevel.ERROR));
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

                serverChannel = b.bind(port).sync().channel();
                log.info("HttpFileServer.start() done, port:" + port);
                serverChannel.closeFuture().sync();
            } catch (InterruptedException ex) {
                log.error("HttpFileServer.start() error!", ex);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("HttpFileServer.start() error!", ex);
            } finally {
                shutdownServer();
            }
        });
    }

    /**
     * stop
     */
    public static void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }

        executor.shutdown();
        shutdownServer();
    }

    /**
     * shutdownServer
     */
    private static void shutdownServer() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
