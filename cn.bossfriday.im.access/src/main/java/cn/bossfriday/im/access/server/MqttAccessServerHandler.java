package cn.bossfriday.im.access.server;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.access.common.enums.DisconnectReason;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

import static cn.bossfriday.im.access.common.AccessContextAttributeKey.*;

/**
 * MqttAccessServerHandler
 *
 * @author chenx
 */
public class MqttAccessServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private IMqttListener listener;

    public MqttAccessServerHandler(IMqttListener listener) {
        if (Objects.isNull(listener)) {
            throw new ServiceRuntimeException("MqttListener is null!");
        }

        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.channel().attr(IS_CHANNEL_ACTIVE).set(true);
        this.listener.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        this.listener.closed(ctx);
        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.listener.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        if (Objects.isNull(msg)) {
            return;
        }

        switch (msg.getType()) {
            case CONNECT:
                this.handleConnectMessage((ConnectMessage) msg, ctx);
                break;
            case PUBLISH:
                this.handlePublishMessage((PublishMessage) msg, ctx);
                break;
            case PUBACK:
                this.handlePubAckMessage((PubAckMessage) msg, ctx);
                break;
            case QUERY:
                this.handleQueryMessage((QueryMessage) msg, ctx);
                break;
            case QUERYCON:
                this.handleQueryConMessage((QueryConMessage) msg, ctx);
                break;
            case PINGREQ:
                this.handlePingReqMessage((PingReqMessage) msg, ctx);
                break;
            case DISCONNECT:
                this.handleDisconnectMessage((DisconnectMessage) msg, ctx);
                break;
            default:
                break;
        }
    }

    /**
     * handleConnectMessage
     */
    private void handleConnectMessage(ConnectMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isChannelActive = ctx.channel().attr(IS_CHANNEL_ACTIVE).get();
        if (!isChannelActive) {
            ctx.close();
            return;
        }

        this.listener.onConnectMessage(msg, ctx);
        ctx.channel().attr(IS_CONNECTED).set(true);
    }


    /**
     * handlePublishMessage
     */
    private void handlePublishMessage(PublishMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        this.listener.onPublishMessage(msg, ctx);
    }

    /**
     * handlePubAckMessage
     */
    private void handlePubAckMessage(PubAckMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        this.listener.onPubAckMessage(msg, ctx);
    }

    /**
     * handleQueryMessage
     */
    private void handleQueryMessage(QueryMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        this.listener.onQueryMessage(msg, ctx);
    }

    /**
     * handleQueryConMessage
     */
    private void handleQueryConMessage(QueryConMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        this.listener.onQueryConMessage(msg, ctx);
    }

    /**
     * handlePingReqMessage
     */
    private void handlePingReqMessage(PingReqMessage msg, ChannelHandlerContext ctx) throws Exception {
        this.listener.onPingReqMessage(msg, ctx);
    }

    /**
     * handleDisconnectMessage
     */
    private void handleDisconnectMessage(DisconnectMessage msg, ChannelHandlerContext ctx) throws Exception {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        ctx.channel().attr(DISCONNECT_REASON).set(DisconnectReason.SDK_DISCONNECT_MESSAGE.getValue());
        this.listener.onDisconnectMessage(msg, ctx);
    }
}
