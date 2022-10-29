package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * FileServerHttpResponseHelper
 *
 * @author chenx
 */
@Slf4j
public class FileServerHttpResponseHelper {

    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();
    private static final String PNG = "png";
    private static final String FIREFOX = "Firefox";
    private static final String CHROME = "Chrome";

    private FileServerHttpResponseHelper() {

    }

    /**
     * sendResponse
     *
     * @param fileTransactionId
     * @param status
     * @param contentType
     * @param responseContent
     * @param isCloseChannel
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
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        } else {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        }

        if (isKeepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        ChannelFuture future = ctx.channel().writeAndFlush(response);
        if (!isKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        removeContext(fileTransactionId, ctx, isCloseChannel);
    }

    /**
     * sendResponse
     *
     * @param fileTransactionId
     * @param status
     * @param responseContent
     */
    public static void sendResponse(String fileTransactionId, HttpResponseStatus status, String responseContent) {
        sendResponse(fileTransactionId, status, "", responseContent, true);
    }

    /**
     * getContentType
     *
     * @param fileName
     * @return
     */
    public static String getContentType(String fileName) {
        String fileExtName = FileUtil.getFileExt(fileName);
        // MimetypesFileTypeMap目前缺少png类型
        if (fileExtName.equals(PNG)) {
            return "image/png";
        } else {
            return MIMETYPES_FILE_TYPE_MAP.getContentType(fileName);
        }
    }

    /**
     * encodedDownloadFileName
     * 原文件名浏览器下载支持
     *
     * @param agent
     * @param fileName
     * @return
     * @throws Base64DecodingException
     * @throws UnsupportedEncodingException
     */
    public static String encodedDownloadFileName(String agent, String fileName) throws Base64DecodingException, UnsupportedEncodingException {
        if (StringUtils.isEmpty(agent)) {
            return fileName;
        }

        String result = "";
        if (-1 != agent.indexOf(FIREFOX)) {
            result = "=?UTF-8?B?" + (new String(Base64.decode(fileName.getBytes(StandardCharsets.UTF_8)))) + "?=";
        } else if (-1 != agent.indexOf(CHROME)) {
            result = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        } else {
            result = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }

        return result;
    }

    /**
     * removeContext
     *
     * @param fileTransactionId
     * @param ctx
     * @param isCloseChannel
     */
    private static void removeContext(String fileTransactionId, ChannelHandlerContext ctx, boolean isCloseChannel) {
        try {
            FileTransactionContextManager.getInstance().removeContext(fileTransactionId);

            if (isCloseChannel) {
                ctx.channel().close();
            }
        } catch (Exception ex) {
            log.error("removeContext error!", ex);
        }
    }
}
