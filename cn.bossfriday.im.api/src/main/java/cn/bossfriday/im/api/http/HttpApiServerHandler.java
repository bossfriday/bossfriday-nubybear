package cn.bossfriday.im.api.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpApiServerHandler
 *
 * @author chenx
 */
@Slf4j
public class HttpApiServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest httpRequest = null;
        try {
            if (msg instanceof FullHttpRequest) {
                httpRequest = (FullHttpRequest) msg;
                this.onMessageReceived(ctx, httpRequest);
            }
        } finally {
            if (httpRequest != null && httpRequest.refCnt() > 0) {
                httpRequest.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("HttpApiServerHandler.exceptionCaught()", cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }

    /**
     * onMessageReceived
     */
    private void onMessageReceived(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

    }
}
