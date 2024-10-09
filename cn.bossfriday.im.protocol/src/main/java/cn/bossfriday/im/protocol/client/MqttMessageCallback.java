package cn.bossfriday.im.protocol.client;

import cn.bossfriday.im.protocol.message.ConnAckMessage;
import cn.bossfriday.im.protocol.message.PubAckMessage;
import cn.bossfriday.im.protocol.message.PublishMessage;
import cn.bossfriday.im.protocol.message.QueryAckMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * MqttMessageCallback
 *
 * @author chenx
 */
public class MqttMessageCallback {

    /**
     * PublishCallback
     */
    public abstract static class PublishCallback extends MessageCallback {

        /**
         * process
         *
         * @param ack
         * @param ctx
         */
        protected abstract void process(PubAckMessage ack, ChannelHandlerContext ctx);
    }

    /**
     * QueryCallback
     */
    public abstract static class QueryCallback extends MessageCallback {

        /**
         * process
         *
         * @param ack
         * @param ctx
         */
        protected abstract void process(QueryAckMessage ack, ChannelHandlerContext ctx);
    }

    /**
     * ConnectCallback
     */
    public abstract static class ConnectCallback extends MessageCallback {

        protected long beginLogin = 0L;

        protected ConnectCallback(String userId) {
            super(userId);
        }

        protected ConnectCallback() {
            super();
            this.beginLogin = System.currentTimeMillis();
        }

        /**
         * process
         *
         * @param ack
         * @param ctx
         */
        protected abstract void process(ConnAckMessage ack, ChannelHandlerContext ctx);
    }

    /**
     * ReceivePublishMessageListener
     */
    public interface ReceivePublishMessageListener {

        /**
         * onMessageReceived
         *
         * @param msg
         * @param ctx
         */
        void onMessageReceived(PublishMessage msg, ChannelHandlerContext ctx);
    }
}
