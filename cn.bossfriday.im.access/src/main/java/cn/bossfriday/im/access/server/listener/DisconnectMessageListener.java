package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.common.enums.DisconnectReason;
import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.DisconnectMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.access.server.AccessContextAttributeKey.DISCONNECT_REASON;
import static cn.bossfriday.im.access.server.AccessContextAttributeKey.IS_CONNECTED;

/**
 * DisconnectMessageListener
 *
 * @author chenx
 */
@Slf4j
public class DisconnectMessageListener extends BaseMqttMessageListener<DisconnectMessage> {

    public DisconnectMessageListener(DisconnectMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(DisconnectMessage msg, ChannelHandlerContext ctx) {
        boolean isConnected = ctx.channel().attr(IS_CONNECTED).get();
        if (!isConnected) {
            ctx.close();
            return;
        }

        ctx.channel().attr(DISCONNECT_REASON).set(DisconnectReason.CLIENT_DISCONNECT_MESSAGE.getValue());
    }
}
