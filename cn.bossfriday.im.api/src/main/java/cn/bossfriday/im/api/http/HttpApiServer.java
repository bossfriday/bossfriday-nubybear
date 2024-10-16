package cn.bossfriday.im.api.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpApiServer
 *
 * @author chenx
 */
@Slf4j
public class HttpApiServer {

    private final int port;
    private Channel channel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private static final int MAX_QUEUE = 8192;
    private static final int MAX_CONTENT_LENGTH = 10 * 1024 * 1024;
    private static final int TIMEOUT_SECONDS = 15;

    private final CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
            .allowNullOrigin()
            .allowedRequestHeaders("*")
            .allowedRequestMethods(HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.GET)
            .maxAge(600)
            .build();

    public HttpApiServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    /**
     * start
     */
    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, MAX_QUEUE)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new ReadTimeoutHandler(TIMEOUT_SECONDS));
                        pipeline.addLast(new WriteTimeoutHandler(TIMEOUT_SECONDS));
                        pipeline.addLast("decoder", new HttpRequestDecoder());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                        pipeline.addLast("encoder", new HttpResponseEncoder());
                        pipeline.addLast("cors", new CorsHandler(HttpApiServer.this.corsConfig));
                        pipeline.addLast("handler", new HttpApiServerHandler());
                    }
                });

        this.channel = b.bind(this.port).sync().channel();
        log.info("HttpApiServer start done, port={}", this.port);
    }

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
