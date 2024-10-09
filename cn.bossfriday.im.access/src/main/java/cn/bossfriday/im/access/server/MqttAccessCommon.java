package cn.bossfriday.im.access.server;

import cn.bossfriday.im.access.common.entities.ClientInfo;
import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.message.ConnectMessage;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * MqttAccessCommon
 *
 * @author chenx
 */
public class MqttAccessCommon {

    private MqttAccessCommon() {
        // do nothing
    }

    /**
     * getClientIp
     *
     * @param msg
     * @param ctx
     * @return
     */
    public static String getClientIp(ConnectMessage msg, ChannelHandlerContext ctx) {
        String clientIp = "";
        if (Objects.nonNull(msg) && StringUtils.isNotEmpty(msg.getClientIp())) {
            clientIp = msg.getClientIp();
            return clientIp;
        }

        if (StringUtils.isEmpty(clientIp) && Objects.nonNull(ctx)) {
            clientIp = ctx.channel().remoteAddress().toString();
        }

        return clientIp;
    }

    /**
     * getClientInfo
     *
     * @param input
     * @return
     */
    public static ClientInfo getClientInfo(String input) {
        return null;
    }

    /**
     * sendConnAck
     *
     * @param ctx
     * @param status
     */
    public static void sendConnAck(ChannelHandlerContext ctx, ConnectionStatus status) {

    }
}
