package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.common.enums.FileUploadType;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.rpc.module.DeleteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.FileDownloadMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

/**
 * HttpFileServerHandler
 *
 * @author chenx
 */
@Slf4j
public class HttpFileServerHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest request;
    private HttpMethod httpMethod;

    private String namespace;
    private String encodedMetaDataIndex;
    private int version = 0;
    private HttpPostRequestDecoder decoder;
    private String fileTransactionId;
    private long offset = 0;

    private long fileTotalSize = 0;
    private boolean isKeepAlive = false;

    /**
     * TODO: 其他上传方式实现使用
     */
    private FileUploadType fileUploadType;
    private long fileSize = 0;

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(false);
    private final StringBuilder errorMsg = new StringBuilder();
    private static final String REG_UPLOAD = "/upload/(.*?)/(.*?)/v(.*?)/";
    private static final String REG_DOWNLOAD = "/" + URL_DOWNLOAD + "/v(.*?)/(.*?)\\.(.*?)";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                this.httpRequestChannelRead(ctx, msg);
            }

            if (msg instanceof HttpObject) {
                this.httpObjectChannelRead(msg);
            }
        } catch (Exception ex) {
            log.error("channelRead error: " + this.fileTransactionId, ex);
            this.errorMsg.append(ex.getMessage());
        } finally {
            if (msg instanceof LastHttpContent) {
                this.lastHttpContentChannelRead();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.abnormallyDeleteTmpFile();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught: " + this.fileTransactionId, cause);
        ctx.channel().close();
        this.abnormallyDeleteTmpFile();
    }

    /**
     * download 文件下载
     */
    private void download() throws IOException {
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(this.version);
        MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(this.encodedMetaDataIndex);
        FileDownloadMsg ms = FileDownloadMsg.builder()
                .fileTransactionId(this.fileTransactionId)
                .metaDataIndex(metaDataIndex)
                // 首次分片下载不传值（第1次分片下载完成后会从元数据中读取，然后缓存至FileTransactionContext供后续分片下载使用
                .fileTotalSize(-1L)
                .chunkIndex(0L)
                .build();
        StorageTracker.getInstance().onDownloadRequestReceived(ms);
    }

    /**
     * 文件上传
     */
    private void fileUpload(HttpObject msg) {
        if (this.decoder == null) {
            return;
        }

        HttpContent chunk = null;
        try {
            if (msg instanceof HttpContent) {
                chunk = (HttpContent) msg;
                this.decoder.offer(chunk);

                if (!this.hasError()) {
                    this.chunkedReadHttpData();
                }
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            this.errorMsg.append(ex.getMessage());
        } finally {
            if (chunk != null) {
                chunk.release();
            }
        }
    }

    /**
     * chunkedReadHttpData 分片读取数据
     * Netty PartialHttpData 使用很容易造成ByteBuf直接内存泄露
     * 调试时通过-Dio.netty.leakDetectionLevel=PARANOID保障对每次请求做检测
     */
    private void chunkedReadHttpData() {
        try {
            while (this.decoder.hasNext()) {
                /**
                 * Returns the next available InterfaceHttpData or null if, at the time it is called,
                 * there is no more available InterfaceHttpData. A subsequent call to offer(httpChunk) could enable more data.
                 * Be sure to call ReferenceCounted.release() after you are done with processing to make sure to not leak any resources
                 */
                InterfaceHttpData data = this.decoder.next();
                if (data instanceof FileUpload) {
                    this.chunkedProcessHttpData((FileUpload) data);
                }
            }

            /**
             * Returns the current InterfaceHttpData if currently in decoding status,
             * meaning all data are not yet within, or null if there is no InterfaceHttpData currently in decoding status
             * (either because none yet decoded or none currently partially decoded).
             * Full decoded ones are accessible through hasNext() and next() methods.
             */
            HttpData data = (HttpData) this.decoder.currentPartialHttpData();
            if (data instanceof FileUpload) {
                this.chunkedProcessHttpData((FileUpload) data);
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            log.info("chunkedReadHttpData end: " + this.fileTransactionId);
        } catch (Throwable tr) {
            log.error("HttpFileServerHandler.chunkedReadHttpData() error!", tr);
            this.errorMsg.append(tr.getMessage());
        }
    }

    /**
     * chunkedProcessHttpData 分片处理数据
     *
     * @param data
     */
    private void chunkedProcessHttpData(FileUpload data) {
        byte[] chunkData = null;
        try {
            ByteBuf byteBuf = data.getByteBuf();
            int readBytesCount = byteBuf.readableBytes();
            chunkData = new byte[readBytesCount];
            byteBuf.readBytes(chunkData);

            WriteTmpFileMsg msg = new WriteTmpFileMsg();
            msg.setStorageEngineVersion(this.version);
            msg.setFileTransactionId(this.fileTransactionId);
            msg.setNamespace(this.namespace);
            msg.setKeepAlive(this.isKeepAlive);
            msg.setFileName(URLDecoder.decode(data.getFilename(), "UTF-8"));
            msg.setFileSize(this.fileSize);
            msg.setFileTotalSize(this.fileTotalSize);
            msg.setOffset(this.offset);
            msg.setData(chunkData);
            StorageTracker.getInstance().onPartialUploadDataReceived(msg);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.chunkedProcessHttpData() error!", ex);
            this.errorMsg.append(ex.getMessage());
        } finally {
            if (chunkData != null) {
                this.offset += chunkData.length;
            }

            // just help GC
            chunkData = null;

            if (data.refCnt() > 0) {
                data.release();
            }
        }
    }

    /**
     * reset
     */
    private void reset() {
        try {
            this.request = null;
            if (this.decoder != null) {
                this.decoder.destroy();
                this.decoder = null;
            }

            log.info("reset done: " + this.fileTransactionId);
        } catch (Exception e) {
            log.error("HttpFileServerHandler.reset() error!", e);
        }
    }

    /**
     * getHeaderValue
     *
     * @param key
     * @return
     */
    private String getHeaderValue(String key) {
        if (this.request.headers().contains(key)) {
            return this.request.headers().get(key);
        }

        return null;
    }

    /**
     * getFileTotalSize
     *
     * @return
     */
    private Long getFileTotalSize() {
        try {
            String fileSizeString = this.getHeaderValue(HEADER_FILE_TOTAL_SIZE);
            if (StringUtils.isEmpty(fileSizeString)) {
                throw new BizException("upload request must contains  " + HEADER_FILE_TOTAL_SIZE + " header");
            }

            return Long.parseLong(fileSizeString);
        } catch (Exception ex) {
            this.errorMsg.append(ex.getMessage());
        }

        throw new BizException("upload request must contains valid " + HEADER_FILE_TOTAL_SIZE + " header!");
    }

    /**
     * parseUploadUrl 解析上传Url
     */
    private void parseUploadUrl() {
        try {
            String url = this.request.getUri().toLowerCase();
            if (!url.endsWith(URL_DELIMITER)) {
                url += URL_DELIMITER;
            }

            if (!Pattern.matches(REG_UPLOAD, url)) {
                throw new BizException("invalid upload url!");
            }

            Pattern pattern = Pattern.compile(REG_UPLOAD);
            Matcher matcher = pattern.matcher(url);
            String uploadTypeString = "";
            String versionString = "";
            while (matcher.find()) {
                this.namespace = matcher.group(1).toLowerCase().trim();
                uploadTypeString = matcher.group(2).toLowerCase().trim();
                versionString = matcher.group(3).trim();
            }

            if (!StorageEngine.getInstance().getNamespaceMap().containsKey(this.namespace)) {
                throw new BizException("invalid storage namespace");
            }

            if (uploadTypeString.equals(URL_UPLOAD_FULL)) {
                this.fileUploadType = FileUploadType.FULL_UPLOAD;
            } else if (uploadTypeString.equals(URL_UPLOAD_BASE64)) {
                this.fileUploadType = FileUploadType.BASE_64_UPLOAD;
            } else if (uploadTypeString.equals(URL_UPLOAD_RANGE)) {
                this.fileUploadType = FileUploadType.RANGE_UPLOAD;
            } else {
                throw new BizException("invalid upload type!");
            }

            this.setVersion(versionString);
        } catch (Exception ex) {
            this.errorMsg.append(ex.getMessage());
        }
    }

    /**
     * parseDownUrl 解析下载Url
     */
    private void parseDownUrl() {
        try {
            String downUrl = this.request.getUri();
            if (!Pattern.matches(REG_DOWNLOAD, downUrl)) {
                throw new BizException("invalid download url: " + downUrl);
            }

            Pattern pattern = Pattern.compile(REG_DOWNLOAD);
            Matcher matcher = pattern.matcher(downUrl);
            String versionString = "";
            while (matcher.find()) {
                versionString = matcher.group(1).trim();
                this.encodedMetaDataIndex = matcher.group(2).trim();
            }

            this.setVersion(versionString);
        } catch (Exception ex) {
            this.errorMsg.append(ex.getMessage());
        }
    }

    /**
     * hasError
     */
    private boolean hasError() {
        return this.errorMsg.length() > 0;
    }

    /**
     * setVersion
     *
     * @param versionStrValue
     */
    private void setVersion(String versionStrValue) {
        try {
            this.version = Integer.parseInt(versionStrValue);
            if (this.version < DEFAULT_STORAGE_ENGINE_VERSION || this.version > MAX_STORAGE_VERSION) {
                throw new BizException("invalid engine version!");
            }
        } catch (Exception ex) {
            throw new BizException("invalid engine version!");
        }
    }

    /**
     * httpRequestChannelRead
     *
     * @param ctx
     * @param msg
     */
    private void httpRequestChannelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            HttpRequest httpRequest = this.request = (HttpRequest) msg;
            this.isKeepAlive = HttpHeaders.isKeepAlive(httpRequest);
            String userAgent = this.getHeaderValue("USER-AGENT");
            if (StringUtils.isEmpty(this.fileTransactionId)) {
                this.fileTransactionId = UUIDUtil.getShortString();
                FileTransactionContextManager.getInstance().addContext(this.fileTransactionId, ctx, this.isKeepAlive, userAgent);
            }

            if (HttpMethod.GET.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.GET;
                this.parseDownUrl();
                return;
            }

            if (HttpMethod.POST.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.POST;
                this.parseUploadUrl();
                this.fileSize = this.fileTotalSize = this.getFileTotalSize();
                return;
            }

            if (HttpMethod.DELETE.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.DELETE;
                return;
            }

            if (HttpMethod.OPTIONS.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.OPTIONS;
                return;
            }
        } catch (Exception ex) {
            log.error("HttpRequest process error!", ex);
            this.errorMsg.append(ex.getMessage());
        } finally {
            if (this.httpMethod.equals(HttpMethod.POST)) {
                try {
                    this.decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, this.request);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    log.warn("getHttpDecoder Error:" + e1.getMessage());
                }
            }
        }
    }

    /**
     * httpObjectChannelRead
     *
     * @param msg
     */
    private void httpObjectChannelRead(Object msg) throws IOException {
        if (this.httpMethod.equals(HttpMethod.POST)) {
            try {
                this.fileUpload((HttpObject) msg);
            } catch (Exception ex) {
                log.error("HttpObject process error!", ex);
                this.errorMsg.append(ex.getMessage());
            }
        } else if (this.httpMethod.equals(HttpMethod.GET)) {
            if (msg instanceof LastHttpContent && !this.hasError()) {
                this.download();
            }
        } else {
            if (msg instanceof LastHttpContent) {
                this.errorMsg.append("unsupported http method");
            }
        }
    }

    /**
     * lastHttpContentChannelRead
     */
    private void lastHttpContentChannelRead() {
        this.reset();
        if (this.hasError()) {
            FileServerHttpResponseHelper.sendResponse(this.fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, this.errorMsg.toString());
        }
    }

    /**
     * 异常情况下临时文件删除
     */
    private void abnormallyDeleteTmpFile() {
        if (StringUtils.isEmpty(this.fileTransactionId)) {
            return;
        }

        StorageTracker.getInstance().onDeleteTmpFileMsg(DeleteTmpFileMsg.builder()
                .fileTransactionId(this.fileTransactionId)
                .storageEngineVersion(this.version)
                .build());
        log.info("abnormallyDeleteTmpFile() done, fileTransactionId:" + this.fileTransactionId);
    }
}
