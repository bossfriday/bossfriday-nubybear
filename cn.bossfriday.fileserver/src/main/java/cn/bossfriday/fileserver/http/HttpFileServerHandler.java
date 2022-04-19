package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.common.enums.FileUploadType;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

@Slf4j
public class HttpFileServerHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest request;
    private FileUploadType fileUploadType;
    private int version = 0;
    private HttpPostRequestDecoder decoder;
    private String fileTransactionId;
    private long offset = 0;
    private long fileSize = 0;  // todo：断点上传使用
    private long fileTotalSize = 0;
    private boolean isKeepAlive = false;
    private ActorRef httpFileServerActor;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(false);
    private final String uploadUrlReg = "/upload/(.*?)/v(.*?)/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                fileTransactionId = UUIDUtil.getShortString();
                httpFileServerActor = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf(ACTOR_HTTP_FILE_SERVER);

                if (HttpMethod.GET.equals(request.method())) {
                    // todo: 文件下载
                } else if (HttpMethod.POST.equals(request.method())) {
                    parseUploadUrl();
                    fileSize = fileTotalSize = getFileTotalSize();
                    isKeepAlive = HttpHeaders.isKeepAlive(request);
                    FileTransactionContextManager.getInstance().addContext(fileTransactionId, ctx, fileSize, fileTotalSize);
                    decoder = new HttpPostRequestDecoder(factory, request);
                } else if (HttpMethod.DELETE.equals(request.method())) {
                    // todo:文件删除
                } else if (HttpMethod.OPTIONS.equals(request.method())) {
                    // todo:doOptions
                } else {
                    throw new Exception("unsupported http method");
                }
            }

            if (msg instanceof HttpObject) {
                fileUpload(ctx, (HttpObject) msg);
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler error!", ex);
            // todo send error http response;
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

        try {
            if (msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                decoder.offer(chunk);

                chunkedReadHttpData();

                if (chunk instanceof LastHttpContent) {
                    release();
                }
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            release();
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
                    HttpData httpData = (HttpData) data;
                    if (httpData instanceof FileUpload) {
                        chunkedProcessHttpData((FileUpload) httpData);
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
            RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(this.fileTransactionId, ACTOR_WRITE_TMP_FILE, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, httpFileServerActor);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.chunkedProcessHttpData() error!", ex);
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
            throw new Exception("upload request must contains  " + HEADER_FILE_TOTAL_SIZE + " header");

        try {
            return Long.parseLong(fileSizeString);
        } catch (Exception e) {
            throw new Exception("invalid " + HEADER_FILE_TOTAL_SIZE + " header value");
        }
    }

    /**
     * 解析上传Url
     */
    private void parseUploadUrl() throws Exception {
        final String url = request.getUri().toLowerCase();
        if (!Pattern.matches(uploadUrlReg, url)) {
            throw new Exception("invalid upload url!");
        }

        Pattern pattern = Pattern.compile(uploadUrlReg);
        Matcher matcher = pattern.matcher(url);
        String uploadTypeString = "";
        String versionString = "";
        while (matcher.find()) {
            uploadTypeString = matcher.group(1).trim();
            versionString = matcher.group(2).trim();
        }

        if (uploadTypeString.equals(URL_UPLOAD_FULL)) {
            fileUploadType = FileUploadType.FullUpload;
        } else if (uploadTypeString.equals(URL_UPLOAD_BASE64)) {
            fileUploadType = FileUploadType.Base64Upload;
        } else if (uploadTypeString.equals(URL_UPLOAD_RANGE)) {
            fileUploadType = FileUploadType.RangeUpload;
        } else {
            throw new Exception("invalid upload url!");
        }

        try {
            version = Integer.parseInt(versionString);
        } catch (Exception ex) {
            throw new Exception("invalid upload url!");
        }
    }
}
