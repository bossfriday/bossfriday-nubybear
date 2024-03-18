package cn.bossfriday.im.protocol;

import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.message.PublishMessage;
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
    public static abstract class PublishCallback extends MessageCallback {

        /**
         * process
         *
         * @param status
         * @param serverTime
         * @param msgUid
         * @param ctx
         */
        protected abstract void process(int status, int serverTime, String msgUid, ChannelHandlerContext ctx);

        @Override
        protected void readTimedOut(ChannelHandlerContext ctx) {
            super.readTimedOut(ctx);
        }
    }

    /**
     * QueryCallback
     */
    public static abstract class QueryCallback extends MessageCallback {

        /**
         * process
         *
         * @param status
         * @param data
         * @param serverTime
         * @param ctx
         */
        protected abstract void process(int status, byte[] data, int serverTime, ChannelHandlerContext ctx);

        @Override
        protected void readTimedOut(ChannelHandlerContext ctx) {
            super.readTimedOut(ctx);
        }
    }

    /**
     * ConnectCallback
     */
    public static abstract class ConnectCallback extends MessageCallback {

        public long beginLogin = 0L;

        public ConnectCallback(String userId) {
            super(userId);
        }

        public ConnectCallback() {
            super();
            this.beginLogin = System.currentTimeMillis();
        }

        /**
         * process
         *
         * @param status
         * @param userId
         * @throws Exception
         */
        protected abstract void process(ConnectionStatus status, String userId) throws Exception;

        @Override
        protected void readTimedOut(ChannelHandlerContext ctx) {
            super.readTimedOut(ctx);
        }
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
         * @throws Exception
         */
        void onMessageReceived(PublishMessage msg, ChannelHandlerContext ctx) throws Exception;
    }
}
