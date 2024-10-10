package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.PubAckMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * PubAckMessageListener
 *
 * @author chenx
 */
public class PubAckMessageListener extends BaseMqttMessageListener<PubAckMessage> {

    public PubAckMessageListener(PubAckMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(PubAckMessage msg, ChannelHandlerContext ctx) {
        // ..
    }
}
