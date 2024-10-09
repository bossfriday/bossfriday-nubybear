package cn.bossfriday.im.access.server;

import cn.bossfriday.common.exception.ServiceException;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.access.server.core.MqttMessageListener;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

import static cn.bossfriday.im.access.server.AccessContextAttributeKey.IS_CHANNEL_ACTIVE;
import static cn.bossfriday.im.access.server.AccessContextAttributeKey.SESSION_ID;

/**
 * MqttAccessServerHandler
 *
 * @author chenx
 */
@Slf4j
public class MqttAccessServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        if (Objects.isNull(msg)) {
            throw new ServiceRuntimeException("MqttMessage Is Null!");
        }

        switch (msg.getType()) {
            case CONNECT:
            case PUBLISH:
            case PUBACK:
            case QUERY:
            case QUERYCON:
            case PINGREQ:
            case DISCONNECT:
                this.onMessageReceived(msg, ctx);
                break;
            default:
                throw new ServiceRuntimeException("Unsupported Message Type: " + msg.getType());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.channel().attr(IS_CHANNEL_ACTIVE).set(true);
        ctx.channel().attr(SESSION_ID).set(UUIDUtil.getShortString(UUID.randomUUID()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MqttAccessServerServerListener exceptionCaught!", cause);
        ctx.channel().close();
    }

    /**
     * onMessageReceived
     */
    private void onMessageReceived(MqttMessage msg, ChannelHandlerContext ctx) throws ServiceException {
        try {
            MqttMessageListener messageListener = MqttMessageListenerFactory.getMqttMessageListener(msg, ctx);
            messageListener.onMessageReceived();
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }
}
