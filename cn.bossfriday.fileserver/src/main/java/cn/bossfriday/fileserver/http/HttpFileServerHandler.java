package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.http.url.UrlParser;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.fileserver.actors.module.DeleteTmpFileMsg;
import cn.bossfriday.fileserver.actors.module.FileDownloadMsg;
import cn.bossfriday.fileserver.actors.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.common.HttpFileServerHelper;
import cn.bossfriday.fileserver.common.enums.FileUploadType;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import static cn.bossfriday.fileserver.actors.module.FileDownloadMsg.FIRST_CHUNK_INDEX;
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
    private Map<String, String> pathArgsMap;
    private Map<String, String> queryArgsMap;
    private HttpPostRequestDecoder decoder;
    private String fileTransactionId;
    private String storageNamespace;
    private FileUploadType fileUploadType;

    private String metaDataIndexString;

    private StringBuilder errorMsg = new StringBuilder();
    private int version = 0;
    private long offset = 0;
    private long filePartitionSize = 0;
    private long fileTotalSize = 0;
    private boolean isKeepAlive = false;

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(false);
    private static final UrlParser uploadUrlParser = new UrlParser("/{" + URI_ARGS_NAME_UPLOAD_TYPE + "}/{" + URI_ARGS_NAME_ENGINE_VERSION + "}/{" + URI_ARGS_NAME_STORAGE_NAMESPACE + "}");
    private static final UrlParser downloadUrlParser = new UrlParser("/" + URL_RESOURCE + "/{" + URI_ARGS_NAME_ENGINE_VERSION + "}/{" + URI_ARGS_NAME_META_DATA_INDEX_STRING + "}");

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
     * httpRequestChannelRead
     *
     * @param ctx
     * @param msg
     */
    private void httpRequestChannelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            HttpRequest httpRequest = this.request = (HttpRequest) msg;
            this.isKeepAlive = HttpUtil.isKeepAlive(httpRequest);
            if (StringUtils.isEmpty(this.fileTransactionId)) {
                this.fileTransactionId = UUIDUtil.getShortString();
                FileTransactionContextManager.getInstance().addContext(this.fileTransactionId, ctx, this.isKeepAlive, this.request.headers().get("USER-AGENT"));
            }

            if (HttpMethod.GET.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.GET;
                this.parseUrl(downloadUrlParser);

                this.metaDataIndexString = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_META_DATA_INDEX_STRING);
                return;
            }

            if (HttpMethod.POST.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.POST;
                this.parseUrl(uploadUrlParser);

                this.filePartitionSize = Long.parseLong(HttpFileServerHelper.getHeaderValue(this.request, HttpHeaderNames.CONTENT_LENGTH.toString()));
                this.fileTotalSize = Long.parseLong(HttpFileServerHelper.getHeaderValue(this.request, HEADER_FILE_TOTAL_SIZE));
                this.storageNamespace = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_STORAGE_NAMESPACE);
                this.fileUploadType = FileUploadType.getByName(this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_UPLOAD_TYPE));
                return;
            }

            if (HttpMethod.DELETE.equals(httpRequest.method())) {
                this.httpMethod = HttpMethod.DELETE;
                this.parseUrl(downloadUrlParser);

                this.metaDataIndexString = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_META_DATA_INDEX_STRING);
                return;
            }

            throw new BizException("unsupported HttpMethod!");
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
    private void httpObjectChannelRead(Object msg) {
        if (this.httpMethod.equals(HttpMethod.POST)) {
            try {
                if (this.fileUploadType == FileUploadType.FULL_UPLOAD || this.fileUploadType == FileUploadType.RANGE_UPLOAD) {
                    this.fileUpload((HttpObject) msg);
                } else if (this.fileUploadType == FileUploadType.BASE_64_UPLOAD) {
                    this.base64Upload((HttpObject) msg);
                } else {
                    throw new BizException("unimplemented upload type!");
                }
            } catch (Exception ex) {
                log.error("HttpObject process error!", ex);
                this.errorMsg.append(ex.getMessage());
            }
        } else if (this.httpMethod.equals(HttpMethod.GET)) {
            if (msg instanceof LastHttpContent && !this.hasError()) {
                this.fileDownload();
            }
        } else if (this.httpMethod.equals(HttpMethod.DELETE)) {
            if (msg instanceof LastHttpContent && !this.hasError()) {
                this.deleteFile();
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
            HttpFileServerHelper.sendResponse(this.fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, this.errorMsg.toString());
        }
    }

    /**
     * fileUpload
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
     * base64Upload
     *
     * @param msg
     */
    private void base64Upload(HttpObject msg) {
        // just to do..
    }

    /**
     * fileDownload
     */
    private void fileDownload() {
        try {
            IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(this.version);
            MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(this.metaDataIndexString);
            FileDownloadMsg fileDownloadMsg = FileDownloadMsg.builder()
                    .fileTransactionId(this.fileTransactionId)
                    .metaDataIndex(metaDataIndex)
                    .chunkIndex(FIRST_CHUNK_INDEX)
                    .build();
            StorageTracker.getInstance().onDownloadRequestReceived(fileDownloadMsg);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileDownload() error!", ex);
            throw new BizException("File download error!");
        }
    }

    /**
     * 文件标记删除
     */
    private void deleteFile() {
        // 文件标记删除
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
            msg.setStorageNamespace(this.storageNamespace);
            msg.setKeepAlive(this.isKeepAlive);
            msg.setFileName(URLDecoder.decode(data.getFilename(), "UTF-8"));
            msg.setFilePartitionSize(this.filePartitionSize);
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
     * parseUrl
     *
     * @param urlParser
     */
    private void parseUrl(UrlParser urlParser) {
        try {
            URI uri = new URI(this.request.uri());
            this.pathArgsMap = urlParser.parsePath(uri);
            this.queryArgsMap = urlParser.parseQuery(uri);
            this.version = HttpFileServerHelper.parserEngineVersionString(UrlParser.getArgsValue(this.pathArgsMap, URI_ARGS_NAME_ENGINE_VERSION));
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.parseUrl() error!", ex);
            this.errorMsg.append(ex.getMessage());
        }
    }

    /**
     * getUrlArgValue
     *
     * @param argMap
     * @param key
     * @return
     */
    private String getUrlArgValue(Map<String, String> argMap, String key) {
        try {
            return UrlParser.getArgsValue(argMap, key);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.getUrlArgValue() error!", ex);
            this.errorMsg.append(ex.getMessage());
        }

        return null;
    }

    /**
     * hasError
     */
    private boolean hasError() {
        return this.errorMsg.length() > 0;
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
