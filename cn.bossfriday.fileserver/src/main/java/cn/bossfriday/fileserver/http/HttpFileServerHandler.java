package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.http.RangeParser;
import cn.bossfriday.common.http.UrlParser;
import cn.bossfriday.common.http.model.Range;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.utils.FileServerUtils;
import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import cn.bossfriday.im.common.enums.file.FileUploadType;
import cn.bossfriday.im.common.message.file.FileDeleteInput;
import cn.bossfriday.im.common.message.file.FileDownloadInput;
import cn.bossfriday.im.common.message.file.WriteTmpFileInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static cn.bossfriday.im.common.constant.FileServerConstant.*;
import static cn.bossfriday.im.common.message.file.FileDownloadInput.FIRST_CHUNK_INDEX;

/**
 * HttpFileServerHandler
 * <p>
 * 备注：
 * 为了对服务端内存占用更加友好，不使用Http聚合（HttpObjectAggregator），
 * 如果使用HttpObjectAggregator则只需对一个FullHttpRequest进行读取即可，处理上会简单很多。
 * 不使用Http聚合一个完整的Http请求会进行1+N次读取：
 * 1、一次HttpRequest读取；
 * 2、N次HttpContent读取：后续处理中通过保障线程一致性去实现文件写入的零拷贝+顺序写
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
    private byte[] base64AggregatedData;
    private String metaDataIndexString;
    private Range range;

    private StringBuilder errorMsg = new StringBuilder();
    private int version = DEFAULT_STORAGE_ENGINE_VERSION;
    private long tempFilePartialDataOffset = 0;
    private long fileTotalSize = 0;
    private int base64AggregateIndex = 0;
    private boolean isKeepAlive = false;

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(false);
    private static final UrlParser UPLOAD_URL_PARSER = new UrlParser("/{" + URI_ARGS_NAME_UPLOAD_TYPE + "}/{" + URI_ARGS_NAME_ENGINE_VERSION + "}/{" + URI_ARGS_NAME_STORAGE_NAMESPACE + "}");
    private static final UrlParser DOWNLOAD_URL_PARSER = new UrlParser("/" + URL_RESOURCE + "/{" + URI_ARGS_NAME_ENGINE_VERSION + "}/{" + URI_ARGS_NAME_META_DATA_INDEX_STRING + "}");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                this.httpRequestRead(ctx, (HttpRequest) msg);
            }

            if (msg instanceof HttpContent) {
                this.httpContentRead((HttpContent) msg);
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught: " + this.fileTransactionId, cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }

        // 异常情况临时文件删除
        FileServerUtils.abnormallyDeleteTmpFile(this.fileTransactionId, this.version);
    }

    /**
     * httpRequestRead
     *
     * @param ctx
     * @param httpRequest
     */
    private void httpRequestRead(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        try {
            this.request = httpRequest;
            this.isKeepAlive = HttpUtil.isKeepAlive(httpRequest);
            this.fileTransactionId = FileServerUtils.getFileTransactionId(httpRequest);
            FileTransactionContextManager.getInstance().registerContext(this.fileTransactionId, ctx, this.isKeepAlive, this.request.headers().get("USER-AGENT"));

            if (HttpMethod.GET.equals(httpRequest.method())) {
                this.parseUrl(DOWNLOAD_URL_PARSER);
                this.httpMethod = HttpMethod.GET;
                this.metaDataIndexString = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_META_DATA_INDEX_STRING);

                return;
            }

            if (HttpMethod.POST.equals(httpRequest.method())) {
                this.parseUrl(UPLOAD_URL_PARSER);
                this.httpMethod = HttpMethod.POST;
                this.storageNamespace = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_STORAGE_NAMESPACE);
                this.fileUploadType = FileUploadType.getByName(this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_UPLOAD_TYPE));

                if (this.fileUploadType == FileUploadType.FULL_UPLOAD) {
                    // 全量上传
                    this.fileTotalSize = Long.parseLong(FileServerUtils.getHeaderValue(this.request, HEADER_FILE_TOTAL_SIZE));
                } else if (this.fileUploadType == FileUploadType.BASE_64_UPLOAD) {
                    // Base64上传
                    int contentLength = Integer.parseInt(FileServerUtils.getHeaderValue(this.request, String.valueOf(HttpHeaderNames.CONTENT_LENGTH)));
                    this.base64AggregatedData = new byte[contentLength];
                } else if (this.fileUploadType == FileUploadType.RANGE_UPLOAD) {
                    // 断点上传
                    this.fileTotalSize = Long.parseLong(FileServerUtils.getHeaderValue(this.request, HEADER_FILE_TOTAL_SIZE));
                    this.range = RangeParser.parseAndGetFirstRange(FileServerUtils.getHeaderValue(this.request, HttpHeaderNames.RANGE.toString()));
                }

                return;
            }

            if (HttpMethod.DELETE.equals(httpRequest.method())) {
                this.parseUrl(DOWNLOAD_URL_PARSER);
                this.httpMethod = HttpMethod.DELETE;
                this.metaDataIndexString = this.getUrlArgValue(this.pathArgsMap, URI_ARGS_NAME_META_DATA_INDEX_STRING);

                return;
            }

            throw new ServiceRuntimeException("unsupported HttpMethod!");
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
     * httpContentRead
     * Netty ByteBuf直接内存溢出问题需要重点关注，
     * 调试时可以通过增加：-Dio.netty.leakDetectionLevel=PARANOID来保障对每次请求都做内存溢出检测
     *
     * @param httpContent
     */
    private void httpContentRead(HttpContent httpContent) {
        try {
            if (this.httpMethod.equals(HttpMethod.POST)) {
                if (this.fileUploadType == FileUploadType.BASE_64_UPLOAD) {
                    this.base64Upload(httpContent);
                } else {
                    this.fileUpload(httpContent);
                }
            } else if (this.httpMethod.equals(HttpMethod.GET)) {
                this.fileDownload(httpContent);
            } else if (this.httpMethod.equals(HttpMethod.DELETE)) {
                this.deleteFile(httpContent);
            } else {
                if (httpContent instanceof LastHttpContent) {
                    this.errorMsg.append("unsupported http method");
                }
            }
        } finally {
            if (httpContent.refCnt() > 0) {
                httpContent.release();
            }
        }
    }

    /**
     * lastHttpContentChannelRead
     */
    private void lastHttpContentChannelRead() {
        this.resetHttpRequest();

        if (this.base64AggregatedData != null) {
            this.base64AggregatedData = null;
        }

        if (this.hasError()) {
            FileServerUtils.abnormallyDeleteTmpFile(this.fileTransactionId, this.version);
            FileServerUtils.sendResponse(this.fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, this.errorMsg.toString());
        }
    }

    /**
     * fileUpload
     */
    private void fileUpload(HttpContent httpContent) {
        if (this.decoder == null) {
            return;
        }

        try {
            /**
             * Initialized the internals from a new chunk
             * content – the new received chunk
             */
            this.decoder.offer(httpContent);
            if (!this.hasError()) {
                this.chunkedFileUpload();
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            this.errorMsg.append(ex.getMessage());
        }
    }

    /**
     * chunkedFileUpload（文件分片上传）
     */
    private void chunkedFileUpload() {
        try {
            while (this.decoder.hasNext()) {
                /**
                 * Returns the next available InterfaceHttpData or null if, at the time it is called,
                 * there is no more available InterfaceHttpData. A subsequent call to offer(httpChunk) could enable more data.
                 * Be sure to call ReferenceCounted.release() after you are done with processing to make sure to not leak any resources
                 */
                InterfaceHttpData data = this.decoder.next();
                if (data instanceof FileUpload) {
                    this.currentPartialHttpDataProcess((FileUpload) data);
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
                this.currentPartialHttpDataProcess((FileUpload) data);
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException endOfDataDecoderException) {
            log.error("HttpFileServerHandler.chunkedFileUpload() EndOfDataDecoderException!");
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.chunkedFileUpload() error!", ex);
            this.errorMsg.append(ex.getMessage());
        }
    }

    /**
     * currentPartialHttpDataProcess
     *
     * @param currentPartialData
     */
    private void currentPartialHttpDataProcess(FileUpload currentPartialData) {
        byte[] partialData = null;
        try {
            ByteBuf byteBuf = currentPartialData.getByteBuf();
            int readBytesCount = byteBuf.readableBytes();
            partialData = new byte[readBytesCount];
            byteBuf.readBytes(partialData);

            WriteTmpFileInput msg = new WriteTmpFileInput();
            msg.setStorageEngineVersion(this.version);
            msg.setFileTransactionId(this.fileTransactionId);
            msg.setStorageNamespace(this.storageNamespace);
            msg.setKeepAlive(this.isKeepAlive);
            msg.setFileName(URLDecoder.decode(currentPartialData.getFilename(), StandardCharsets.UTF_8.name()));
            msg.setRange(this.range);
            msg.setFileTotalSize(this.fileTotalSize);
            msg.setOffset(this.tempFilePartialDataOffset);
            msg.setData(partialData);
            StorageTracker.getInstance().onPartialUploadDataReceived(msg);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.chunkedProcessFileUpload() error!", ex);
            this.errorMsg.append(ex.getMessage());
        } finally {
            if (partialData != null) {
                this.tempFilePartialDataOffset += partialData.length;
            }

            if (currentPartialData.refCnt() > 0) {
                currentPartialData.release();
            }

            // just help GC
            partialData = null;
        }
    }

    /**
     * base64Upload
     * 由于对不完整Base64信息进行解码可能失败，因此Base64上传处理方式为聚合完成后进行Base64解码然后再进行全量上传
     * 这是base64Upload只能用于例如截屏等小文件的上传场景的原因。
     *
     * @param httpContent
     */
    private void base64Upload(HttpContent httpContent) {
        ByteBuf byteBuf = null;
        byte[] currentPartialData = null;
        byte[] decodedFullData = null;
        try {
            // 数据聚合
            byteBuf = httpContent.content();
            int currentPartialDataLength = byteBuf.readableBytes();
            currentPartialData = new byte[currentPartialDataLength];
            byteBuf.readBytes(currentPartialData);
            System.arraycopy(currentPartialData, 0, this.base64AggregatedData, this.base64AggregateIndex, currentPartialDataLength);
            this.base64AggregateIndex += currentPartialDataLength;

            // 聚合完成
            if (httpContent instanceof LastHttpContent) {
                decodedFullData = Base64.decodeBase64(this.base64AggregatedData);

                WriteTmpFileInput msg = new WriteTmpFileInput();
                msg.setStorageEngineVersion(this.version);
                msg.setFileTransactionId(this.fileTransactionId);
                msg.setStorageNamespace(this.storageNamespace);
                msg.setKeepAlive(this.isKeepAlive);
                msg.setFileName(this.fileTransactionId + "." + this.getUrlArgValue(this.queryArgsMap, URI_ARGS_NAME_EXT));
                msg.setRange(this.range);
                msg.setFileTotalSize(decodedFullData.length);
                msg.setOffset(this.tempFilePartialDataOffset);
                msg.setData(decodedFullData);
                StorageTracker.getInstance().onPartialUploadDataReceived(msg);
            }
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }

            // just help GC
            currentPartialData = null;
            decodedFullData = null;
        }
    }

    /**
     * fileDownload
     *
     * @param httpContent
     */
    private void fileDownload(HttpContent httpContent) {
        if (httpContent instanceof LastHttpContent && !this.hasError()) {
            try {
                IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(this.version);
                MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(this.metaDataIndexString);
                FileDownloadInput fileDownloadInput = FileDownloadInput.builder()
                        .fileTransactionId(this.fileTransactionId)
                        .metaDataIndex(metaDataIndex)
                        .chunkIndex(FIRST_CHUNK_INDEX)
                        .build();
                StorageTracker.getInstance().onDownloadRequestReceived(fileDownloadInput);
            } catch (Exception ex) {
                log.error("HttpFileServerHandler.fileDownload() error!", ex);
                throw new ServiceRuntimeException("File download error!");
            }
        }
    }

    /**
     * deleteFile
     *
     * @param httpContent
     */
    private void deleteFile(HttpContent httpContent) {
        if (httpContent instanceof LastHttpContent && !this.hasError()) {
            try {
                IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(this.version);
                MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(this.metaDataIndexString);
                FileDeleteInput msg = FileDeleteInput.builder()
                        .fileTransactionId(this.fileTransactionId)
                        .metaDataIndex(metaDataIndex)
                        .build();
                StorageTracker.getInstance().onFileDeleteMsg(msg);
            } catch (Exception ex) {
                log.error("HttpFileServerHandler.deleteFile() error!", ex);
                throw new ServiceRuntimeException("delete file error!");
            }
        }
    }

    /**
     * resetHttpRequest
     */
    private void resetHttpRequest() {
        try {
            this.request = null;
            if (this.decoder != null) {
                this.decoder.destroy();
                this.decoder = null;
            }

            log.info("HttpFileServerHandler.resetHttpRequest() done: " + this.fileTransactionId);
        } catch (Exception e) {
            log.error("HttpFileServerHandler.resetHttpRequest() error!", e);
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
            this.version = FileServerUtils.parserEngineVersionString(UrlParser.getArgsValue(this.pathArgsMap, URI_ARGS_NAME_ENGINE_VERSION));
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
}
