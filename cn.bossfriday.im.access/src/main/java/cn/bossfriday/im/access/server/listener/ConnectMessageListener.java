package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.common.enums.ConnectState;
import cn.bossfriday.im.access.server.MqttAccessCommon;
import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.common.codec.ImTokenCodec;
import cn.bossfriday.im.common.entity.ImToken;
import cn.bossfriday.im.protocol.client.ClientInfo;
import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.message.ConnectMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static cn.bossfriday.im.access.server.AccessContextAttributeKey.*;

/**
 * ConnectMessageListener
 *
 * @author chenx
 */
@Slf4j
public class ConnectMessageListener extends BaseMqttMessageListener<ConnectMessage> {

    public static final long TTL_TOKEN = 30 * 24 * 3600 * 1000L;

    public ConnectMessageListener(ConnectMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(ConnectMessage msg, ChannelHandlerContext ctx) {
        try {
            boolean isChannelActive = ctx.channel().attr(IS_CHANNEL_ACTIVE).get();
            if (!isChannelActive) {
                ctx.close();
                return;
            }

            ctx.channel().attr(CONN_STATE).set(ConnectState.CONN);

            // 解析will
            ClientInfo clientInfo = ClientInfo.fromWill(msg.getWill());
            if (Objects.isNull(clientInfo) || Objects.isNull(clientInfo.getClientType())) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.IDENTIFIER_REJECTED, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            ctx.channel().attr(CLIENT_INFO).set(clientInfo);

            // Token校验
            ImToken imToken = ImTokenCodec.decode(msg.getToken());
            if (Objects.isNull(imToken)) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.INVALID_TOKEN, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            if (!imToken.getDeviceId().equalsIgnoreCase(clientInfo.getDeviceId())) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.DEVICE_ERROR, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            if ((System.currentTimeMillis() - imToken.getTime()) >= TTL_TOKEN) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.TOKEN_EXPIRE, ConnectState.CONN_ACK_FAILURE);
                return;
            }


            String uid = imToken.getUserId();
            String clientIp = MqttAccessCommon.getClientIp(msg, ctx);
            String deviceId = clientInfo.getDeviceId();
            log.info("[%User Connected done%], uid: {}, ip: {}, deviceId: {}", uid, clientIp, deviceId);
            ctx.channel().attr(IS_CONNECTED).set(true);
        } catch (Exception ex) {
            log.error("ConnectMessageListener.onMqttMessageReceived() error!", ex);
            MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.SERVICE_ERROR, ConnectState.CONN_ACK_FAILURE);
        }
    }
}
