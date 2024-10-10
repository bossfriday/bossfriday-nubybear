package cn.bossfriday.im.access.server;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.access.server.listener.*;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.message.*;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * MqttMessageListenerFactory
 *
 * @author chenx
 */
public class MqttMessageListenerFactory {

    private MqttMessageListenerFactory() {
        // do nothing
    }

    /**
     * getMqttMessageListener
     *
     * @param msg
     * @param ctx
     * @return
     */
    @SuppressWarnings("squid:S1452")
    public static BaseMqttMessageListener<?> getMqttMessageListener(MqttMessage msg, ChannelHandlerContext ctx) {
        if (Objects.isNull(msg)) {
            throw new ServiceRuntimeException("MqttMessage is Null!");
        }

        if (msg instanceof ConnectMessage) {
            return new ConnectMessageListener((ConnectMessage) msg, ctx);
        }

        if (msg instanceof PublishMessage) {
            return new PublishMessageListener((PublishMessage) msg, ctx);
        }

        if (msg instanceof PubAckMessage) {
            return new PubAckMessageListener((PubAckMessage) msg, ctx);
        }

        if (msg instanceof QueryMessage) {
            return new QueryMessageListener((QueryMessage) msg, ctx);
        }

        if (msg instanceof QueryConMessage) {
            return new QueryConMessageListener((QueryConMessage) msg, ctx);
        }

        if (msg instanceof PingReqMessage) {
            return new PingReqMessageListener((PingReqMessage) msg, ctx);
        }

        if (msg instanceof DisconnectMessage) {
            return new PingReqMessageListener((PingReqMessage) msg, ctx);
        }

        throw new ServiceRuntimeException("Unsupported MqttMessageListener! (MqttMessageType: " + msg.getType() + ")");
    }
}
