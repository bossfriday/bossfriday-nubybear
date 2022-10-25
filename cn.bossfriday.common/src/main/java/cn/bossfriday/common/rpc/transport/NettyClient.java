package cn.bossfriday.common.rpc.transport;

import com.google.common.collect.EvictingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NettyClient
 *
 * @author chenx
 */
@Slf4j
public class NettyClient {

    private static final int QUEUE_FIX_SIZE = 10000;
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;
    private static final int RECONNECT_INTERVAL_SECOND = 5;

    private Bootstrap bootstrap;
    private Channel channel;

    private String host;
    private int port;

    /**
     * 使用有限队列防止异常下OOM
     */
    private final EvictingQueue<RpcMessage> sendQueue;
    private AtomicReference<ConnStatus> connState;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.sendQueue = EvictingQueue.create(QUEUE_FIX_SIZE);
        this.connState = new AtomicReference<>(ConnStatus.CLOSE);
    }

    /**
     * connect
     */
    public void connect() {
        if (this.connState.get() == ConnStatus.CONNECTING || this.connState.get() == ConnStatus.CONNECTED) {
            return;
        }

        this.connState.set(ConnStatus.CONNECTING);
        NettyClient client = this;
        this.bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        this.bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcDecoder())
                                .addLast(new RpcEncoder())
                                .addLast(new WriteTimeoutHandler(300))
                                .addLast(new NettyClientHandler(client));
                    }
                });

        ChannelFuture future = this.bootstrap.connect(this.host, this.port);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                log.warn("NettyClient.connect() failed, prepare to reconnect, target:" + this.host + ":" + this.port);
                this.connState.set(ConnStatus.CLOSE);
                final EventLoop loop = channelFuture.channel().eventLoop();
                loop.schedule(new Runnable() {
                    @Override
                    public void run() {
                        NettyClient.this.connect();
                    }
                }, RECONNECT_INTERVAL_SECOND, TimeUnit.SECONDS);

                return;
            }

            log.info("NettyClient.connect() success, target:" + this.host + ":" + this.port);
            this.channel = channelFuture.channel();
            this.connState.set(ConnStatus.CONNECTED);

            // add close listener
            this.channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                // 如果连接被动关闭,一直重连
                this.connState.set(ConnStatus.CLOSE);
                this.connect();
            });

            this.consumeQueue();
        });
    }

    /**
     * send
     *
     * @param message
     */
    public void send(RpcMessage message) {
        switch (this.connState.get()) {
            case CLOSE:
            case CONNECTING:
                this.connect();
                this.insertToQueue(message);
                break;
            case CONNECTED:
                this.write(message);
                break;
            case CLOSED:
                log.warn("ignore send message, connState:CLOSED");
                break;
            default:
                log.warn("ignore send message, unimplemented ConnStatus!");
        }
    }

    /**
     * close
     */
    public void close() {
        if (this.channel != null) {
            this.connState.set(ConnStatus.CLOSED);
            this.channel.close();
            this.channel = null;
            this.bootstrap = null;
        }
    }

    /**
     * consumeQueue
     */
    private void consumeQueue() {
        if (this.sendQueue.isEmpty()) {
            return;
        }

        RpcMessage message;
        while ((message = this.sendQueue.poll()) != null) {
            this.write(message);
        }
    }

    /**
     * insertToQueue
     *
     * @param message
     */
    private void insertToQueue(RpcMessage message) {
        if (this.sendQueue.size() == QUEUE_FIX_SIZE) {
            log.warn("The sendQueue is full and discard an old msg, session:=" + message.getSessionString());
        }

        this.sendQueue.offer(message);
    }

    /**
     * write
     *
     * @param message
     */
    private void write(RpcMessage message) {
        if (!this.channel.isActive()) {
            this.connState.set(ConnStatus.CLOSE);
            this.insertToQueue(message);
            return;
        }

        try {
            message.buildTimestamp();
            this.channel.writeAndFlush(message).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("NettyClient.write() error, target:" + this.host + ":" + this.port, future.cause());
                }
            });
        } catch (Exception e) {
            log.error("NettyClient.process() error!", e);
        }
    }

    enum ConnStatus {
        /**
         * 正在连接
         */
        CONNECTING,

        /**
         * 已连接
         */
        CONNECTED,

        /**
         * 连接关闭（需要重新建立连接）
         */
        CLOSE,

        /**
         * 连接被主动关闭
         */
        CLOSED
    }
}
