package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.common.enums.FileUploadType;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.bossfriday.fileserver.common.FileServerConst.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpFileServerHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest request;
    private FileUploadType fileUploadType;  // todo：断点上传使用
    private String namespace;
    private int version = 0;
    private HttpPostRequestDecoder decoder;
    private String fileTransactionId;
    private long offset = 0;
    private long fileSize = 0;  // todo：断点上传使用
    private long fileTotalSize = 0;
    private boolean isKeepAlive = false;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(false);
    private final StringBuilder errorMsg = new StringBuilder();
    private final String uploadUrlReg = "/upload/(.*?)/(.*?)/v(.*?)/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            try {
                log.info("-------------HttpRequest begin-------------");
                HttpRequest request = this.request = (HttpRequest) msg;
                isKeepAlive = HttpHeaders.isKeepAlive(request);
                if (StringUtils.isEmpty(this.fileTransactionId)) {
                    fileTransactionId = UUIDUtil.getShortString();
                }

                if (HttpMethod.GET.equals(request.method())) {
                    // todo: 文件下载
                    return;
                }

                if (HttpMethod.POST.equals(request.method())) {
                    parseUploadUrl();
                    fileSize = fileTotalSize = getFileTotalSize();
                    FileTransactionContextManager.getInstance().addContext(version, fileTransactionId, ctx, fileSize, fileTotalSize);
                    return;
                }

                if (HttpMethod.DELETE.equals(request.method())) {
                    // todo:文件删除
                    return;
                }

                if (HttpMethod.OPTIONS.equals(request.method())) {
                    // todo:doOptions
                    return;
                }

                errorMsg.append("unsupported http method");
            } catch (Exception ex) {
                log.error("HttpRequest process error!", ex);
                errorMsg.append(ex.getMessage());
            } finally {
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    errorMsg.append(e1.getMessage());
                    sendErrorResponse(ctx);
                }
            }
        }

        if (msg instanceof HttpObject) {
            try {
                fileUpload(ctx, (HttpObject) msg);
            } catch (Exception ex) {
                log.error("HttpObject process error!", ex);
                errorMsg.append(ex.getMessage());
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
            decoder.destroy();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("HttpFileServerHandler.exceptionCaught()", cause);
        release();
        ctx.channel().close();
    }

    /**
     * 文件上传
     */
    private void fileUpload(ChannelHandlerContext ctx, HttpObject msg) {
        if (decoder == null) {
            return;
        }

        HttpContent chunk = null;
        try {
            if (msg instanceof HttpContent) {
                chunk = (HttpContent) msg;
                decoder.offer(chunk);

                if (!hasError()) {
                    chunkedReadHttpData();
                }
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            errorMsg.append(ex.getMessage());
        } finally {
            if (chunk instanceof LastHttpContent) {
                if (hasError()) {
                    sendErrorResponse(ctx);
                }

                release();
            }
        }
    }

    /**
     * 分片读取数据
     */
    private void chunkedReadHttpData() {
        try {
            while (decoder.hasNext()) {
                /**
                 * Returns the next available InterfaceHttpData or null if, at the time it is called,
                 * there is no more available InterfaceHttpData. A subsequent call to offer(httpChunk) could enable more data.
                 * Be sure to call ReferenceCounted.release() after you are done with processing to make sure to not leak any resources
                 */
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        if (data instanceof FileUpload) {
                            chunkedProcessHttpData((FileUpload) data);
                        }
                    } finally {
                        data.release();
                    }
                }
            }

            /**
             * Returns the current InterfaceHttpData if currently in decoding status,
             * meaning all data are not yet within, or null if there is no InterfaceHttpData currently in decoding status
             * (either because none yet decoded or none currently partially decoded).
             * Full decoded ones are accessible through hasNext() and next() methods.
             */
            HttpData data = (HttpData) decoder.currentPartialHttpData();
            if (data != null && data instanceof FileUpload) {
                chunkedProcessHttpData((FileUpload) data);
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            log.info(this.fileTransactionId + " chunk end");
        } catch (Throwable tr) {
            log.error("HttpFileServerHandler.chunkedReadHttpData() error!", tr);
            errorMsg.append(tr.getMessage());
        }
    }

    /**
     * 分片处理数据
     */
    private void chunkedProcessHttpData(FileUpload data) {
        byte[] chunkData = null;
        try {
            ByteBuf byteBuf = data.getByteBuf();
            int readBytesCount = byteBuf.readableBytes();
            chunkData = new byte[readBytesCount];
            byteBuf.readBytes(chunkData);

            WriteTmpFileMsg msg = new WriteTmpFileMsg();
            msg.setStorageEngineVersion(version);
            msg.setFileTransactionId(this.fileTransactionId);
            msg.setNamespace(namespace);
            msg.setKeepAlive(isKeepAlive);
            msg.setFileName(data.getFilename());
            msg.setFileSize(fileSize);
            msg.setFileTotalSize(fileTotalSize);
            msg.setOffset(offset);
            msg.setData(chunkData);
            StorageTracker.getInstance().onPartialUploadDataReceived(msg);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.chunkedProcessHttpData() error!", ex);
            errorMsg.append(ex.getMessage());
        } finally {
            offset += chunkData.length;
            chunkData = null;
        }
    }

    /**
     * 释放所有资源
     */
    private void release() {
        try {
            request = null;
            if (decoder != null) {
                decoder.destroy();
                decoder = null;
            }
        } catch (Exception e) {
            log.error("HttpFileServerHandler.release() error!", e);
        }
    }

    /**
     * getHeaderValue
     */
    private String getHeaderValue(String key) {
        if (request.headers().contains(key)) {
            return request.headers().get(key);
        }

        return null;
    }

    /**
     * getFileTotalSize
     */
    private Long getFileTotalSize() throws Exception {
        String fileSizeString = getHeaderValue(HEADER_FILE_TOTAL_SIZE);
        if (StringUtils.isEmpty(fileSizeString))
            throw new BizException("upload request must contains  " + HEADER_FILE_TOTAL_SIZE + " header");

        try {
            return Long.parseLong(fileSizeString);
        } catch (Exception e) {
            throw new BizException("invalid " + HEADER_FILE_TOTAL_SIZE + " header value");
        }
    }

    /**
     * 解析上传Url
     */
    private void parseUploadUrl() throws Exception {
        final String url = request.getUri().toLowerCase();
        if (!Pattern.matches(uploadUrlReg, url)) {
            throw new BizException("invalid upload url!");
        }

        Pattern pattern = Pattern.compile(uploadUrlReg);
        Matcher matcher = pattern.matcher(url);
        String uploadTypeString = "";
        String versionString = "";
        while (matcher.find()) {
            namespace = matcher.group(1).toLowerCase().trim();
            uploadTypeString = matcher.group(2).toLowerCase().trim();
            versionString = matcher.group(3).trim();
        }

        if (!StorageEngine.getInstance().getNamespaceMap().containsKey(namespace)) {
            throw new BizException("invalid storage namespace");
        }

        if (uploadTypeString.equals(URL_UPLOAD_FULL)) {
            fileUploadType = FileUploadType.FullUpload;
        } else if (uploadTypeString.equals(URL_UPLOAD_BASE64)) {
            fileUploadType = FileUploadType.Base64Upload;
        } else if (uploadTypeString.equals(URL_UPLOAD_RANGE)) {
            fileUploadType = FileUploadType.RangeUpload;
        } else {
            throw new BizException("invalid upload type!");
        }

        try {
            version = Integer.parseInt(versionString);
            if (version < DEFAULT_STORAGE_ENGINE_VERSION || version > MAX_STORAGE_VERSION)
                throw new BizException("invalid engine version!");
        } catch (Exception ex) {
            throw new BizException("invalid engine version!");
        }
    }

    /**
     * hasError
     */
    private boolean hasError() {
        return errorMsg.length() > 0;
    }

    /**
     * sendErrorResponse
     */
    private void sendErrorResponse(ChannelHandlerContext ctx) {
        if (!FileTransactionContextManager.getInstance().existed(fileTransactionId)) {
            FileServerHttpResponseHelper.sendErrorResponse(ctx, errorMsg.toString());
            return;
        }

        FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMsg.toString());
    }
}
