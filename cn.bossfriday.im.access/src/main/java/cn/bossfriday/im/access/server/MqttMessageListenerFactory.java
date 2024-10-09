package cn.bossfriday.im.access.server;

import cn.bossfriday.im.access.server.core.MqttMessageListener;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.channel.ChannelHandlerContext;

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
    public static MqttMessageListener getMqttMessageListener(MqttMessage msg, ChannelHandlerContext ctx) {
        return null;
    }
}
