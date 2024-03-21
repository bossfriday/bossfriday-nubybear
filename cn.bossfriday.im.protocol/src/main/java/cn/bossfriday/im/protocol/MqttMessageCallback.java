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
    public abstract static class PublishCallback extends MessageCallback {

        /**
         * process
         *
         * @param status
         * @param serverTime
         * @param msgUid
         * @param ctx
         */
        protected abstract void process(int status, int serverTime, String msgUid, ChannelHandlerContext ctx);
    }

    /**
     * QueryCallback
     */
    public abstract static class QueryCallback extends MessageCallback {

        /**
         * process
         *
         * @param status
         * @param data
         * @param serverTime
         * @param ctx
         */
        protected abstract void process(int status, byte[] data, int serverTime, ChannelHandlerContext ctx);
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
         * @param status
         * @param userId
         * @throws Exception
         */
        protected abstract void process(ConnectionStatus status, String userId);
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
