package cn.bossfriday.im.api.http;

import cn.bossfriday.im.api.common.enums.ApiRequestType;
import cn.bossfriday.im.api.helper.ApiServerResponseHelper;
import cn.bossfriday.im.common.result.ResultCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Objects;

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
        try {
            URI uri = new URI(httpRequest.uri());
            ApiRequestType requestType = ApiRequestType.find(httpRequest.method().name(), uri);
            if (Objects.isNull(requestType)) {
                ApiServerResponseHelper.sendApiResponse(ctx, ResultCode.API_REQUEST_URI_ERROR);
                return;
            }

            ApiServerResponseHelper.sendApiResponse(ctx, ResultCode.OK);
        } catch (Exception ex) {
            log.error("HttpApiServerHandler.onMessageReceived() error!", ex);
            ApiServerResponseHelper.sendApiResponse(ctx, ResultCode.SYSTEM_ERROR);
        }
    }
}
