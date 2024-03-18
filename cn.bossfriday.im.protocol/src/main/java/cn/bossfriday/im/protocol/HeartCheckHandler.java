package cn.bossfriday.im.protocol;

import cn.bossfriday.im.protocol.message.PingReqMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * HeartCheckHandler
 *
 * @author chenx
 */
public class HeartCheckHandler extends IdleStateHandler {

    public HeartCheckHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        ctx.channel().writeAndFlush(new PingReqMessage());
        super.channelIdle(ctx, e);
    }
}
