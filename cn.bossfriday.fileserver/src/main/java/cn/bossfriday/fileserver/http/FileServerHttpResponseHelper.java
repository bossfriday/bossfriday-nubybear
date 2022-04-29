package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.activation.MimetypesFileTypeMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class FileServerHttpResponseHelper {
    private static final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    /**
     * sendResponse
     */
    public static void sendResponse(String fileTransactionId, HttpResponseStatus status, String contentType, String responseContent, boolean isCloseChannel) {
        FileTransactionContext fileCtx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
        if (fileCtx == null) {
            log.warn("FileTransactionContext not existed: " + fileTransactionId);
            return;
        }

        ChannelHandlerContext ctx = fileCtx.getCtx();
        boolean isKeepAlive = fileCtx.isKeepAlive();

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8));
        if (!StringUtils.isEmpty(contentType)) {
            response.headers().set(CONTENT_TYPE, contentType);
        } else {
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        }

        if (isKeepAlive) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        ChannelFuture future = ctx.channel().writeAndFlush(response);
        if (!isKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        removeContext(fileTransactionId, ctx, isCloseChannel);
    }

    public static void sendResponse(String fileTransactionId, HttpResponseStatus status, String responseContent) {
        sendResponse(fileTransactionId, status, "", responseContent, true);
    }

    /**
     * sendErrorResponse
     */
    public static void sendErrorResponse(ChannelHandlerContext ctx, String msg) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
        ctx.channel().close();
    }

    /**
     * getContentType
     */
    public static String getContentType(String fileName) {
        String fileExtName = FileUtil.getFileExt(fileName);
        if (fileExtName.equals("png")) { // MimetypesFileTypeMap目前缺少png类型
            return "image/png";
        } else {
            return mimetypesFileTypeMap.getContentType(fileName);
        }
    }

    /**
     * removeContext
     */
    private static void removeContext(String fileTransactionId, ChannelHandlerContext ctx, boolean isCloseChannel) {
        try {
            FileTransactionContextManager.getInstance().removeContext(fileTransactionId);

            if (isCloseChannel)
                ctx.channel().close();
        } catch (Exception ex) {
            log.error("removeContext error!", ex);
        }
    }
}
