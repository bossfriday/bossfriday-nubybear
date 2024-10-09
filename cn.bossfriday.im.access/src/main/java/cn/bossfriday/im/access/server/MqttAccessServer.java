package cn.bossfriday.im.access.server;

import cn.bossfriday.im.access.server.core.AccessServer;
import cn.bossfriday.im.protocol.codec.MqttMessageDecoder;
import cn.bossfriday.im.protocol.codec.MqttMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.protocol.codec.MqttMessageDecoder.MQTT_CODEC_TIMEOUT;

/**
 * MqttAccessServer
 *
 * @author chenx
 */
@Slf4j
public class MqttAccessServer extends AccessServer {

    public MqttAccessServer(int port) {
        super(port);
    }

    /**
     * 1. SO_RCVBUF（接收缓冲区大小）：
     * 操作系统会根据内存和网络配置决定其默认值。典型的默认值为 64KB 到 256KB，但现代操作系统可能会自动调整它的大小，或者允许动态扩展。
     * 在 Linux 中，你可以通过 /proc/sys/net/core/rmem_default 来查看默认的接收缓冲区大小
     * <p>
     * 2. SO_BACKLOG（连接请求的积压队列大小）：
     * SO_BACKLOG 代表了在服务器处理连接之前，内核允许的最大等待连接数。它决定了可以在连接排队等待的客户端连接数。
     * 默认值依赖于操作系统，通常为 50 到 128。
     * 在 Linux 中，可以通过 /proc/sys/net/core/somaxconn 来查看最大允许的 SO_BACKLOG 值，默认值通常为 128，但有些现代系统可能将其提升到 4096。
     */
    @Override
    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.WARN))
                .option(ChannelOption.SO_BACKLOG, 512)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MqttMessageDecoder(MQTT_CODEC_TIMEOUT));
                        pipeline.addLast(new MqttMessageEncoder());
                        pipeline.addLast(new MqttAccessServerHandler());
                    }
                });

        this.channel = b.bind(this.port).sync().channel();
        log.info("[MqttAccessServer started, port: {}]", this.port);
    }
}
