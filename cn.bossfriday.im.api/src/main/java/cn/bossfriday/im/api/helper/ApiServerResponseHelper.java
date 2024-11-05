package cn.bossfriday.im.api.helper;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.im.common.entity.result.Result;
import cn.bossfriday.im.common.entity.result.ResultCode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.api.common.ApiConstant.HTTP_CONTENT_TYPE_JSON;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * ApiServerResponseHelper
 *
 * @author chenx
 */
@Slf4j
public class ApiServerResponseHelper {

    private ApiServerResponseHelper() {
        // do noting
    }

    /**
     * sendResponse
     *
     * @param ctx
     * @param result
     */
    public static void sendApiResponse(ChannelHandlerContext ctx, Result<?> result) {
        String body = GsonUtil.toJson(result);
        sendResponse(ctx, HttpResponseStatus.OK, HTTP_CONTENT_TYPE_JSON, body);
    }

    /**
     * sendErrorResponse
     *
     * @param ctx
     * @param resultCode
     */
    public static void sendApiResponse(ChannelHandlerContext ctx, ResultCode resultCode) {
        String body = GsonUtil.toJson(Result.error(resultCode));
        sendResponse(ctx, HttpResponseStatus.OK, HTTP_CONTENT_TYPE_JSON, body);
    }

    /**
     * sendResponse
     *
     * @param ctx
     * @param httpResponseStatus
     * @param responseBody
     */
    public static void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus, String contentType, String responseBody) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8));
            response.headers().set("Content-Type", contentType);
            response.headers().set("Content-Length", response.content().readableBytes());
            response.headers().set("Access-Control-Allow-Origin", "*");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception ex) {
            log.error("ApiServerResponseHelper.sendResponse() error!", ex);
        }
    }
}
