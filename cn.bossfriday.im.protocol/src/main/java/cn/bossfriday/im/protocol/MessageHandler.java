package cn.bossfriday.im.protocol;

import cn.bossfriday.im.protocol.MqttMessageCallback.ConnectCallback;
import cn.bossfriday.im.protocol.MqttMessageCallback.PublishCallback;
import cn.bossfriday.im.protocol.MqttMessageCallback.QueryCallback;
import cn.bossfriday.im.protocol.MqttMessageCallback.ReceivePublishMessageListener;
import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.enums.QoS;
import cn.bossfriday.im.protocol.message.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;

/**
 * MessageHandler
 *
 * @author chenx
 */
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private AttributeKey<String> attrKey;
    private ConcurrentHashMap<String, ConnectItem> connectMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PublishItem> publishMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, QueryItem> queryMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MessageListenerItem> listenerMap = new ConcurrentHashMap<>();
    private ChannelHandlerContext ctx;

    public MessageHandler(AttributeKey attrKey) {
        this.attrKey = attrKey;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.channel().close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MqttMessage request)
            throws Exception {
        this.handleMessage(request, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

    /**
     * putQueryCallback
     *
     * @param queryCallback
     * @param queryMessage
     * @param userId
     */
    public void putQueryCallback(QueryCallback queryCallback, QueryMessage queryMessage, String userId) {
        QueryItem item = new QueryItem();
        item.queryCallback = queryCallback;
        item.queryMessage = queryMessage;
        this.queryMap.put(userId + "_" + queryMessage.getMessageSequence(), item);
        if (this.ctx != null) {
            queryCallback.resumeTimer(this.ctx);
        }

    }

    /**
     * putPublishCallback
     *
     * @param publishCallback
     * @param publishMessage
     * @param userId
     */
    public void putPublishCallback(PublishCallback publishCallback, PublishMessage publishMessage, String userId) {
        PublishItem item = new PublishItem();
        item.publishCallback = publishCallback;
        item.publishMessage = publishMessage;
        this.publishMap.put(userId + "_" + publishMessage.getMessageSequence(), item);

        if (this.ctx != null) {
            publishCallback.resumeTimer(this.ctx);
        }
    }

    /**
     * setConnectCallback
     *
     * @param connectCallback
     * @param userId
     */
    public void setConnectCallback(ConnectCallback connectCallback, String userId) {
        ConnectItem item = new ConnectItem();
        item.connectCallback = connectCallback;
        this.connectMap.put(userId, item);

        if (this.ctx != null && this.ctx.channel().isWritable()) {
            connectCallback.resumeTimer(this.ctx);
        }
    }

    /**
     * setReceiveMessageListener
     *
     * @param listener
     * @param userId
     */
    public void setReceiveMessageListener(ReceivePublishMessageListener listener, String userId) {
        MessageListenerItem item = new MessageListenerItem();
        item.listener = listener;
        this.listenerMap.put(userId, item);
    }

    /**
     * handleMessage
     */
    private void handleMessage(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
        if (msg == null) {
            return;
        }

        switch (msg.getType()) {
            case CONNECT:
                // connect
                break;
            case CONNACK:
                this.handleMessage((ConnAckMessage) msg, ctx);
                break;
            case PUBLISH:
                this.handleMessage((PublishMessage) msg, ctx);
                break;
            case PUBACK:
                this.handleMessage((PubAckMessage) msg, ctx);
                break;
            case QUERYACK:
                this.handleMessage((QueryAckMessage) msg, ctx);
                break;
            case QUERYCON:
                //  queryCon
                break;
            case SUBSCRIBE:
                // subscribe
                break;
            case UNSUBSCRIBE:
                // unsubscribe
                break;
            case PINGRESP:
                // pingResp
                break;
            case DISCONNECT:
                this.handleMessage((DisconnectMessage) msg, ctx);
                break;
            default:
                throw new MqttException("invalid messageType!");
        }
    }

    /**
     * handleMessage
     *
     * @param msg
     * @param ctx
     */
    private void handleMessage(DisconnectMessage msg, ChannelHandlerContext ctx) {
        ctx.close();
    }

    /**
     * handleMessage
     *
     * @param msg
     * @param ctx
     * @throws Exception
     */
    private void handleMessage(ConnAckMessage msg, ChannelHandlerContext ctx) throws Exception {
        ConnectItem item = this.connectMap.get(ctx.channel().attr(this.attrKey).get());
        if (item != null) {
            item.connectCallback.readTime = System.currentTimeMillis();
            item.connectCallback.process(msg.getStatus(), msg.getUserId());
        }
    }

    /**
     * handleMessage
     *
     * @param msg
     * @param ctx
     * @throws Exception
     */
    private void handleMessage(PublishMessage msg, ChannelHandlerContext ctx)
            throws Exception {
        MessageListenerItem item = this.listenerMap.get(ctx.channel().attr(this.attrKey).get());

        if (item != null) {
            if (msg.getQos() != QoS.AT_MOST_ONCE) {
                ctx.writeAndFlush(new PubAckMessage(msg.getMessageSequence()));
            }

            item.listener.onMessageReceived(msg, ctx);
        }
    }

    /**
     * handleMessage
     *
     * @param msg
     * @param ctx
     */
    private void handleMessage(QueryAckMessage msg, ChannelHandlerContext ctx) {
        QueryItem item = this.queryMap.get(ctx.channel().attr(this.attrKey).get() + "_" + msg.getMessageSequence());

        if (item != null) {
            this.queryMap.remove(ctx.channel().attr(this.attrKey).get() + "_" + msg.getMessageSequence());

            if (item.queryMessage.getQos() != QoS.AT_MOST_ONCE) {
                ctx.writeAndFlush(new QueryConMessage(msg.getMessageSequence()));
            }
            if (item.queryCallback != null && ctx.channel().isWritable()) {
                if (null == msg.getData()) {
                    return;
                }

                item.queryCallback.process(msg.getStatus(), msg.getData(),
                        msg.getDate(), ctx);
                item.queryCallback.pauseTimer();
            } else {
                if (item.queryCallback == null) {
                    throw new MqttException("QueryAck item.queryCallback is null.");
                } else {
                    throw new MqttException("QueryAck channel is close.");
                }
            }
        } else {
            throw new MqttException("QueryAck item is null.");
        }
    }

    /**
     * handleMessage
     *
     * @param msg
     * @param ctx
     */
    private void handleMessage(PubAckMessage msg, ChannelHandlerContext ctx) {
        PublishItem item = this.publishMap.get(ctx.channel().attr(this.attrKey).get() + "_" + msg.getMessageSequence());

        if (item != null) {
            this.publishMap.remove(ctx.channel().attr(this.attrKey).get() + "_" + msg.getMessageSequence());
            if (item.publishCallback != null && ctx.channel().isWritable()) {
                item.publishCallback.process(msg.getStatus(), msg.getDate(), msg.getMsgUid(), ctx);
                item.publishCallback.pauseTimer();
            } else {
                if (item.publishCallback == null) {
                    throw new MqttException("PubAck item.publishCallback is null.");
                } else {
                    throw new MqttException("HandleMessage channel is close.");
                }
            }
        } else {
            throw new MqttException("PubAck item is null.");
        }
    }

    /**
     * QueryItem
     */
    private static class QueryItem {
        private QueryMessage queryMessage;
        private QueryCallback queryCallback;
    }

    /**
     * PublishItem
     */
    private static class PublishItem {
        private PublishMessage publishMessage;
        private PublishCallback publishCallback;
    }

    /**
     * ConnectItem
     */
    private static class ConnectItem {
        private ConnectCallback connectCallback;
    }

    /**
     * MessageListenerItem
     */
    private class MessageListenerItem {
        private ReceivePublishMessageListener listener;
    }
}