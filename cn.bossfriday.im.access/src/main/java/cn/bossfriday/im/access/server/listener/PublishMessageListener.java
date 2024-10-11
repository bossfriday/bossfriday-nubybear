package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.PublishMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * PublishMessageListener
 *
 * @author chenx
 */
public class PublishMessageListener extends BaseMqttMessageListener<PublishMessage> {

    public PublishMessageListener(PublishMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    public void onMqttMessageReceived() {
        // ..
    }
}
