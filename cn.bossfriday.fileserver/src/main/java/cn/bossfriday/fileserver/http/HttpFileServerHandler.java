package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.common.enums.FileUploadType;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
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
    private FileUploadType fileUploadType;
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
                HttpRequest request = this.request = (HttpRequest) msg;
                fileTransactionId = UUIDUtil.getShortString();
                isKeepAlive = HttpHeaders.isKeepAlive(request);

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
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        log.error("HttpFileServerHandler.exceptionCaught()", cause);
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
            // 读取分片内容
            ByteBuf byteBuf = data.getByteBuf();
            int readBytesCount = byteBuf.readableBytes();
            chunkData = new byte[readBytesCount];
            byteBuf.readBytes(chunkData);

            // RPC处理
            WriteTmpFileMsg msg = new WriteTmpFileMsg();
            msg.setStorageEngineVersion(version);
            msg.setFileTransactionId(this.fileTransactionId);
            msg.setKeepAlive(isKeepAlive);
            msg.setFileName(data.getFilename());
            msg.setFileSize(fileSize);
            msg.setFileTotalSize(fileTotalSize);
            msg.setOffset(offset);
            msg.setData(chunkData);
            RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(this.fileTransactionId, ACTOR_FS_TMP_FILE, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, StorageEngine.getInstance().getStorageTracker());
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
            log.info("HttpFileServerHandler.release() done: " + fileTransactionId);
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
    private Long getFileTotalSize() {
        String fileSizeString = getHeaderValue(HEADER_FILE_TOTAL_SIZE);
        if (StringUtils.isEmpty(fileSizeString))
            throw new RuntimeException("upload request must contains  " + HEADER_FILE_TOTAL_SIZE + " header");

        try {
            return Long.parseLong(fileSizeString);
        } catch (Exception e) {
            throw new RuntimeException("invalid " + HEADER_FILE_TOTAL_SIZE + " header value");
        }
    }

    /**
     * 解析上传Url
     */
    private void parseUploadUrl() {
        final String url = request.getUri().toLowerCase();
        if (!Pattern.matches(uploadUrlReg, url)) {
            throw new RuntimeException("invalid upload url!");
        }

        Pattern pattern = Pattern.compile(uploadUrlReg);
        Matcher matcher = pattern.matcher(url);
        String uploadTypeString = "";
        String versionString = "";
        while (matcher.find()) {
            namespace = matcher.group(1).trim();
            uploadTypeString = matcher.group(2).trim();
            versionString = matcher.group(3).trim();
        }

        if (!StorageEngine.getInstance().getNamespaceMap().containsKey(namespace)) {
            throw new RuntimeException("invalid storage namespace");
        }

        if (uploadTypeString.equals(URL_UPLOAD_FULL)) {
            fileUploadType = FileUploadType.FullUpload;
        } else if (uploadTypeString.equals(URL_UPLOAD_BASE64)) {
            fileUploadType = FileUploadType.Base64Upload;
        } else if (uploadTypeString.equals(URL_UPLOAD_RANGE)) {
            fileUploadType = FileUploadType.RangeUpload;
        } else {
            throw new RuntimeException("invalid upload type!");
        }

        try {
            version = Integer.parseInt(versionString);
        } catch (Exception ex) {
            throw new RuntimeException("invalid engine version!");
        }
    }

    /**
     * sendErrorResponse
     */
    private void sendErrorResponse(ChannelHandlerContext ctx) {
        if (!hasError()) {
            return;
        }

        if (!FileTransactionContextManager.getInstance().existed(fileTransactionId)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(errorMsg.toString(), CharsetUtil.UTF_8));
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            ChannelFuture future = ctx.channel().writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
            ctx.channel().close();

            return;
        }

        FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMsg.toString());
    }

    /**
     * hasError
     */
    private boolean hasError() {
        return errorMsg.length() > 0;
    }
}
