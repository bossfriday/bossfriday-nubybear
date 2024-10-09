package cn.bossfriday.im.protocol.client;

import cn.bossfriday.im.protocol.message.PingReqMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * ClientHeartHandler
 *
 * @author chenx
 */
public class ClientHeartHandler extends IdleStateHandler {

    public ClientHeartHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        ctx.channel().writeAndFlush(new PingReqMessage());
        super.channelIdle(ctx, e);
    }
}
