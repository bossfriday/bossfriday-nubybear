package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.common.entities.ClientInfo;
import cn.bossfriday.im.access.server.MqttAccessCommon;
import cn.bossfriday.im.access.server.core.MqttMessageListener;
import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.message.ConnectMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

import static cn.bossfriday.im.access.server.AccessContextAttributeKey.IS_CHANNEL_ACTIVE;
import static cn.bossfriday.im.access.server.AccessContextAttributeKey.IS_CONNECTED;

/**
 * ConnectMessageListener
 *
 * @author chenx
 */
public class ConnectMessageListener extends MqttMessageListener<ConnectMessage> {

    public ConnectMessageListener(ConnectMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(ConnectMessage msg, ChannelHandlerContext ctx) {
        boolean isChannelActive = ctx.channel().attr(IS_CHANNEL_ACTIVE).get();
        if (!isChannelActive) {
            ctx.close();
            return;
        }

        String clientIp = MqttAccessCommon.getClientIp(msg, ctx);
        ClientInfo clientInfo = MqttAccessCommon.getClientInfo(msg.getWill());
        if (Objects.isNull(clientInfo) || Objects.isNull(clientInfo.getClientType())) {
            MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.IDENTIFIER_REJECTED);
            return;
        }

        ctx.channel().attr(IS_CONNECTED).set(true);
    }
}
