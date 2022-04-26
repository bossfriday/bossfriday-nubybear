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
import cn.bossfriday.fileserver.rpc.module.DownloadMsg;
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
    private static final String REG_UPLOAD = "/upload/(.*?)/(.*?)/v(.*?)/";
    private static final String REG_DOWNLOAD = "/" + URL_DOWNLOAD + "/v(.*?)/(.*?)\\.(.*?)";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                try {
                    log.info("-------------HttpRequest begin-------------");
                    HttpRequest request = this.request = (HttpRequest) msg;
                    isKeepAlive = HttpHeaders.isKeepAlive(request);
                    if (StringUtils.isEmpty(this.fileTransactionId)) {
                        fileTransactionId = UUIDUtil.getShortString();
                        FileTransactionContextManager.getInstance().addContext(fileTransactionId, ctx, isKeepAlive);
                    }

                    if (HttpMethod.GET.equals(request.method())) {
                        download();
                        return;
                    }

                    if (HttpMethod.POST.equals(request.method())) {
                        parseUploadUrl();
                        fileSize = fileTotalSize = getFileTotalSize();
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
                    decoder = new HttpPostRequestDecoder(factory, request);
                }
            }

            if (msg instanceof HttpObject) {
                try {
                    fileUpload((HttpObject) msg);
                } catch (Exception ex) {
                    log.error("HttpObject process error!", ex);
                    errorMsg.append(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("channelRead error: " + fileTransactionId, ex);
            FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive: " + fileTransactionId);
        if (decoder != null) {
            decoder.cleanFiles();
            decoder.destroy();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught: " + fileTransactionId, cause);
        reset();
        ctx.channel().close();
    }

    /**
     * 文件下载
     * TODO:保障同一个下载全过程始终使用同一个线程进行下载
     */
    private void download() throws Exception {
        String downUrl = request.getUri();
        if (!downUrl.endsWith("/"))
            downUrl += "/";

        if (!Pattern.matches(REG_DOWNLOAD, downUrl))
            throw new BizException("invalid download url: " + downUrl);

        Pattern pattern = Pattern.compile(REG_DOWNLOAD);
        Matcher matcher = pattern.matcher(downUrl);
        String encodedMetaDataIndex = "";
        String versionString = "";
        while (matcher.find()) {
            versionString = matcher.group(1).trim();
            encodedMetaDataIndex = matcher.group(2).trim();
        }

        setVersion(versionString);
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(version);
        MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(encodedMetaDataIndex);
        DownloadMsg ms = DownloadMsg.builder()
                .fileTransactionId(fileTransactionId)
                .isKeepAlive(isKeepAlive)
                .metaDataIndex(metaDataIndex)
                .chunkIndex(0L)
                .build();
        StorageTracker.getInstance().onDownloadRequestReceived(ms);
    }

    /**
     * 文件上传
     */
    private void fileUpload(HttpObject msg) {
        if (decoder == null) {
            return;
        }

        HttpContent chunk = null;
        try {
            if (msg instanceof HttpContent) {
                chunk = (HttpContent) msg;
                decoder.offer(chunk);

                if (!hasErrorMsg()) {
                    chunkedReadHttpData();
                }
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            errorMsg.append(ex.getMessage());
        } finally {
            if (chunk instanceof LastHttpContent) {
                if (hasErrorMsg()) {
                    FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMsg.toString());
                }

                reset();
            }

            if(chunk != null) {
                chunk.release();
            }
        }
    }

    /**
     * 分片读取数据
     * Netty PartialHttpData 使用很容易造成ByteBuf直接内存泄露
     * 调试时通过-Dio.netty.leakDetectionLevel=PARANOID保障对每次请求做检测
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
                if (data != null && data instanceof FileUpload) {
                    chunkedProcessHttpData((FileUpload) data);
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
            log.info("chunkedReadHttpData end: " + this.fileTransactionId);
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
            request = null;
            if (decoder != null) {
                decoder.destroy();
                decoder = null;
            }

            log.info("reset done: " + fileTransactionId);
        } catch (Exception e) {
            log.error("HttpFileServerHandler.reset() error!", e);
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
        try {
            String fileSizeString = getHeaderValue(HEADER_FILE_TOTAL_SIZE);
            if (StringUtils.isEmpty(fileSizeString))
                throw new BizException("upload request must contains  " + HEADER_FILE_TOTAL_SIZE + " header");

            try {
                return Long.parseLong(fileSizeString);
            } catch (Exception e) {
                throw new BizException("invalid " + HEADER_FILE_TOTAL_SIZE + " header value");
            }
        } catch (Exception ex) {
            errorMsg.append(ex.getMessage());
        }

        return 0L;
    }

    /**
     * 解析上传Url
     */
    private void parseUploadUrl() {
        try {
            String url = request.getUri().toLowerCase();
            if(!url.endsWith("/")) {
                url += "/";
            }

            if (!Pattern.matches(REG_UPLOAD, url)) {
                throw new BizException("invalid upload url!");
            }

            Pattern pattern = Pattern.compile(REG_UPLOAD);
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

            setVersion(versionString);
        } catch (Exception ex) {
            errorMsg.append(ex.getMessage());
        }
    }

    /**
     * hasError
     */
    private boolean hasErrorMsg() {
        return errorMsg.length() > 0;
    }

    /**
     * setVersion
     */
    private void setVersion(String versionString) throws Exception {
        try {
            version = Integer.parseInt(versionString);
            if (version < DEFAULT_STORAGE_ENGINE_VERSION || version > MAX_STORAGE_VERSION)
                throw new BizException("invalid engine version!");
        } catch (Exception ex) {
            throw new BizException("invalid engine version!");
        }
    }

    public static void main(String[] args) throws Exception {
        String downUrl = "/download/v1/3EGJLnXEkYdv6gFVnFmFVxerAVanann57CsUeKirFMdvCnTwGAwd9C.jpg";

        if (!Pattern.matches(REG_DOWNLOAD, downUrl))
            throw new BizException("invalid download url: " + downUrl);

        Pattern pattern = Pattern.compile(REG_DOWNLOAD);
        Matcher matcher = pattern.matcher(downUrl);
        String encodedMetaDataIndex = "";
        String versionString = "";
        while (matcher.find()) {
            versionString = matcher.group(1).trim();
            encodedMetaDataIndex = matcher.group(2).trim();
        }

        System.out.println(versionString);
        System.out.println(encodedMetaDataIndex);

//        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(version);
//        MetaDataIndex metaDataIndex = metaDataHandler.downloadUrlDecode(encodedMetaDataIndex);
//        DownloadMsg ms = DownloadMsg.builder()
//                .fileTransactionId(fileTransactionId)
//                .isKeepAlive(isKeepAlive)
//                .metaDataIndex(metaDataIndex)
//                .chunkIndex(0L)
//                .build();
    }
}
