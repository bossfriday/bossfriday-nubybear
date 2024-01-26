package cn.bossfriday.fileserver.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.actors.model.DeleteTmpFileMsg;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.engine.enums.FileStatus;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static cn.bossfriday.fileserver.common.FileServerConst.*;


/**
 * FileServerUtils
 *
 * @author chenx
 */
@Slf4j
public class FileServerUtils {

    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();
    private static final String PNG = "png";
    private static final String FIREFOX = "Firefox";
    private static final String CHROME = "Chrome";

    private FileServerUtils() {

    }

    /**
     * parserEngineVersionString
     *
     * @param versionString
     * @return
     */
    public static int parserEngineVersionString(String versionString) {
        final int versionPrefixLength = URL_PREFIX_STORAGE_VERSION.length();
        if (StringUtils.isEmpty(versionString) || versionString.length() <= versionPrefixLength) {
            throw new ServiceRuntimeException(TIP_MSG_INVALID_ENGINE_VERSION);
        }

        int version = 0;
        try {
            version = Integer.parseInt(versionString.substring(versionPrefixLength));
            if (version < DEFAULT_STORAGE_ENGINE_VERSION || version > MAX_STORAGE_VERSION) {
                throw new ServiceRuntimeException(TIP_MSG_INVALID_ENGINE_VERSION);
            }
        } catch (Exception ex) {
            throw new ServiceRuntimeException(TIP_MSG_INVALID_ENGINE_VERSION);
        }

        return version;
    }

    /**
     * getFileTransactionId
     *
     * @param httpRequest
     * @return
     */
    public static String getFileTransactionId(HttpRequest httpRequest) {
        if (httpRequest == null) {
            throw new ServiceRuntimeException("The input http request is null!");
        }

        if (!httpRequest.headers().contains(HEADER_FILE_TRANSACTION_ID)) {
            return UUIDUtil.getShortString();
        }

        String getFromHeaderValue = httpRequest.headers().get(HEADER_FILE_TRANSACTION_ID);
        if (StringUtils.isEmpty(getFromHeaderValue)) {
            throw new ServiceRuntimeException(HEADER_FILE_TRANSACTION_ID + " header value is empty!");
        }

        return getFromHeaderValue;
    }

    /**
     * getHeaderValue
     *
     * @param httpRequest
     * @param headerName
     * @return
     */
    public static String getHeaderValue(HttpRequest httpRequest, String headerName) {
        if (httpRequest == null) {
            throw new ServiceRuntimeException("The input http request is null!");
        }

        if (StringUtils.isEmpty(headerName)) {
            throw new ServiceRuntimeException("The input http header name is empty!");
        }

        if (!httpRequest.headers().contains(headerName)) {
            throw new ServiceRuntimeException("Request must contains valid " + headerName + " header!");
        }

        return httpRequest.headers().get(headerName);
    }

    /**
     * sendResponse
     *
     * @param fileTransactionId
     * @param status
     * @param contentType
     * @param responseContent
     * @param isForceCloseChannel 如果不强制close，则看是否keepAlive
     */
    public static void sendResponse(String fileTransactionId, HttpResponseStatus status, String contentType, String responseContent, boolean isForceCloseChannel) {
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
        boolean isCloseChannel = !isKeepAlive || isForceCloseChannel;
        if (isCloseChannel) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        // unregister fileContext
        FileTransactionContextManager.getInstance().unregisterContext(fileTransactionId);
    }

    /**
     * sendRangeUploadNoContentResponse
     *
     * @param fileTransactionId
     * @param rangeHeaderValue
     */
    public static void sendRangeUploadNoContentResponse(String fileTransactionId, String rangeHeaderValue) {
        FileTransactionContext fileCtx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
        if (fileCtx == null) {
            log.warn("FileTransactionContext not existed: " + fileTransactionId);
            return;
        }

        ChannelHandlerContext ctx = fileCtx.getCtx();
        boolean isKeepAlive = fileCtx.isKeepAlive();

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.CONTENT_RANGE, rangeHeaderValue);

        ChannelFuture future = ctx.channel().writeAndFlush(response);
        if (!isKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        // reset ChannelHandlerContext
        fileCtx.setCtx(null);
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
     * @throws UnsupportedEncodingException
     */
    public static String encodedDownloadFileName(String agent, String fileName) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(agent)) {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }

        String result = "";
        if (-1 != agent.indexOf(FIREFOX)) {
            result = "=?UTF-8?B?" + (new String(Base64.encodeBase64(fileName.getBytes(StandardCharsets.UTF_8)))) + "?=";
        } else if (-1 != agent.indexOf(CHROME)) {
            result = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        } else {
            result = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }

        return result;
    }

    /**
     * abnormallyDeleteTmpFile 异常情况临时文件删除
     *
     * @param fileTransactionId
     * @param version
     */
    public static void abnormallyDeleteTmpFile(String fileTransactionId, int version) {
        if (StringUtils.isEmpty(fileTransactionId)) {
            return;
        }

        StorageTracker.getInstance().onDeleteTmpFileMsg(DeleteTmpFileMsg.builder()
                .fileTransactionId(fileTransactionId)
                .storageEngineVersion(version)
                .build());
        log.info("abnormallyDeleteTmpFile() done, fileTransactionId:" + fileTransactionId);
    }

    /**
     * setFileStatus 设置文件状态标志位
     *
     * @param value
     * @param fileStatus
     * @return
     */
    public static int setFileStatus(int value, FileStatus fileStatus) {
        return fileStatus.getValue() | value;
    }

    /**
     * isFileStatusTrue 判断文件状态标志位
     *
     * @param value
     * @param fileStatus
     * @return
     */
    public static boolean isFileStatusTrue(int value, FileStatus fileStatus) {
        return (value & fileStatus.getValue()) > 0;
    }
}
