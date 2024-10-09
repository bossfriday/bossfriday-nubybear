package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.PingReqMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * PingReqMessageListener
 *
 * @author chenx
 */
public class PingReqMessageListener extends BaseMqttMessageListener<PingReqMessage> {

    public PingReqMessageListener(PingReqMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(PingReqMessage msg, ChannelHandlerContext ctx) {

    }
}
