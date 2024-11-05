package cn.bossfriday.im.access.server;

import cn.bossfriday.im.common.enums.access.ConnectState;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.message.ConnAckMessage;
import cn.bossfriday.im.protocol.message.ConnectMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
     * sendConnAck
     *
     * @param ctx
     * @param status
     */
    public static void sendConnAck(ChannelHandlerContext ctx, ConnectionStatus status, ConnectState conState) {
        if (ctx.channel().attr(AccessContextAttributeKey.CONN_STATE).get() != ConnectState.CONN) {
            return;
        }

        ctx.channel().attr(AccessContextAttributeKey.CONN_STATE).set(conState);

        ConnAckMessage msg = new ConnAckMessage(status);
        MqttAccessCommon.writeAndFlush(msg, ctx)
                .addListener(
                        (ChannelFutureListener) future -> future.channel().close()
                );
    }

    /**
     * writeAndFlush
     *
     * @param msg
     * @param ctx
     * @return
     */
    public static ChannelFuture writeAndFlush(MqttMessage msg, ChannelHandlerContext ctx) {
        return ctx.channel()
                .writeAndFlush(msg)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        future.channel().close();
                    }
                });
    }
}
