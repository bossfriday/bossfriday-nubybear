package cn.bossfriday.common.rpc.mailbox;

import cn.bossfriday.common.rpc.dispatch.ActorDispatcher;
import cn.bossfriday.common.rpc.interfaces.IMsgHandler;
import cn.bossfriday.common.rpc.transport.NettyServer;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfriday.common.common.SystemConstant.SLOW_QUEUE_THRESHOLD;

/**
 * MessageInBox
 *
 * @author chenx
 */
@Slf4j
public class MessageInBox extends BaseMailBox {

    private final NettyServer server;
    private ActorDispatcher dispatcher;

    @SuppressWarnings("squid:S1604")
    public MessageInBox(int size, int port, ActorDispatcher actorDispatcher) {
        super(new LinkedBlockingQueue<>(size));
        this.dispatcher = actorDispatcher;
        this.server = new NettyServer(port, new IMsgHandler() {
            @Override
            public void msgHandle(RpcMessage msg) {
                MessageInBox.super.put(msg);
            }
        });
    }

    @Override
    public void start() {
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            this.server.start(bossGroup, workerGroup);

            super.start();
        } catch (Exception e) {
            log.error("MessageInBox start() error!", e);
        }
    }

    @Override
    public void process(RpcMessage msg) {
        if (msg.getTimestamp() > 0) {
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - msg.getTimestamp() > SLOW_QUEUE_THRESHOLD) {
                log.warn("slow rpc, " + currentTimestamp + " - " + msg.getTimestamp() + " > " + SLOW_QUEUE_THRESHOLD);
            }
        }

        this.dispatcher.dispatch(msg);
    }

    @Override
    public void stop() {
        try {
            super.isStart = false;
            super.queue.clear();

            if (this.server != null) {
                this.server.stop();
            }
        } catch (Exception e) {
            log.error("MessageInBox stop() error!", e);
        }
    }
}
