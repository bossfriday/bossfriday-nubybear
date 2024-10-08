package cn.bossfriday.im.access.server;

import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * MqttAccessServerHandler
 *
 * @author chenx
 */
public class MqttAccessServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private IMqttListener listener;

    public MqttAccessServerHandler(IMqttListener listener) {
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (this.listener != null) {
            this.listener.channelActive(ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (this.listener != null) {
            this.listener.closed(ctx);
        }

        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.listener != null) {
            this.listener.exceptionCaught(ctx, cause);
        }

        ctx.channel().close();
    }
}
