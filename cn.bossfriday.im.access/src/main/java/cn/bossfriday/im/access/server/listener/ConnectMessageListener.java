package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.common.enums.ConnectState;
import cn.bossfriday.im.access.server.MqttAccessCommon;
import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.common.biz.AppRegistrationManager;
import cn.bossfriday.im.common.codec.ImTokenCodec;
import cn.bossfriday.im.common.conf.entity.AppInfo;
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
            ImToken token = ImTokenCodec.decode(msg.getToken());
            if (Objects.isNull(token)) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.INVALID_TOKEN, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            // token与deviceId绑定检查
            if (!token.getDeviceId().equalsIgnoreCase(clientInfo.getDeviceId())) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.DEVICE_ERROR, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            // token过期检查
            if ((System.currentTimeMillis() - token.getTime()) >= TTL_TOKEN) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.TOKEN_EXPIRE, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            // app状态检查
            long appId = token.getAppId();
            boolean isAppOk = AppRegistrationManager.isAppOk(appId);
            if (!isAppOk) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.APP_BLOCK_OR_DELETE, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            // appSecret刷新所有老token失效；
            AppInfo appInfo = AppRegistrationManager.getAppInfo(appId);
            if (token.getAppSecretHash() != AppRegistrationManager.getAppSecretHashCode(appInfo.getAppSecret())) {
                MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.INVALID_TOKEN, ConnectState.CONN_ACK_FAILURE);
                return;
            }

            String uid = token.getUserId();
            String clientIp = MqttAccessCommon.getClientIp(msg, ctx);
            String deviceId = clientInfo.getDeviceId();
            log.info("[%User Connected Done%], appId: {}, uid: {}, ip: {}, deviceId: {}", appId, uid, clientIp, deviceId);
            ctx.channel().attr(IS_CONNECTED).set(true);
        } catch (Exception ex) {
            log.error("ConnectMessageListener.onMqttMessageReceived() error!", ex);
            MqttAccessCommon.sendConnAck(ctx, ConnectionStatus.SERVICE_ERROR, ConnectState.CONN_ACK_FAILURE);
        }
    }
}
