package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.entity.MetaData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.http.FileServerHttpResponseHelper;
import cn.bossfriday.fileserver.rpc.module.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static cn.bossfriday.fileserver.common.FileServerConst.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;

@Slf4j
public class StorageTracker {
    private volatile static StorageTracker instance = null;

    @Getter
    private ActorRef trackerActor;

    private StorageTracker() {
        try {
            trackerActor = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf(ACTOR_FS_TRACKER);
        } catch (Exception ex) {
            log.error("StorageTracker error!", ex);
        }
    }

    /**
     * getInstance
     */
    public static StorageTracker getInstance() {
        if (instance == null) {
            synchronized (StorageTracker.class) {
                if (instance == null) {
                    instance = new StorageTracker();
                }
            }
        }

        return instance;
    }

    /**
     * 临时文件写入请求
     */
    public void onPartialUploadDataReceived(WriteTmpFileMsg msg) throws Exception {
        // 按fileTransactionId路由
        RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(msg.getFileTransactionId(), ACTOR_FS_TMP_FILE, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
    }

    /**
     * 临时文件写入结果
     */
    public void onWriteTmpFileResultReceived(WriteTmpFileResult msg) throws Exception {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
            return;
        }

        // 强制路由：同一个fileTransaction要求在同一个集群节点处理
        RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(msg.getClusterNodeName(), ACTOR_FS_UPLOAD, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
    }

    /**
     * 上传结果
     */
    public void onUploadResultReceived(UploadResult msg) throws Exception {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
            return;
        }

        String fileTransactionId = msg.getFileTransactionId();
        MetaDataIndex metaDataIndex = msg.getMetaDataIndex();
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(metaDataIndex.getStoreEngineVersion());
        String path = metaDataHandler.downloadUrlEncode(metaDataIndex);
        String uploadResponseBody = "{\"rc_url\":{\"path\":\"" + path + "\",\"type\":0}}";
        FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.OK, HttpHeaders.Values.APPLICATION_JSON, uploadResponseBody, false);
        log.info(fileTransactionId + " upload done:" + uploadResponseBody);
    }

    /**
     * 下载请求
     */
    public void onDownloadRequestReceived(DownloadMsg msg) {
        String fileTransactionId = "";
        try {
            // 强制路由：优先从主节点下载
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex index = msg.getMetaDataIndex();
            RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(index.getClusterNode(), ACTOR_FS_DOWNLOAD, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
        } catch (Exception ex) {
            log.error("onDownloadRequestReceived() process error: " + fileTransactionId, ex);
            FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * 下载结果
     */
    public void onDownloadResult(DownloadResult msg) {
        String fileTransactionId = "";
        try {
            final String tid = fileTransactionId = msg.getFileTransactionId();
            if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
                FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
                return;
            }

            FileTransactionContext fileCtx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
            if (fileCtx == null) {
                log.warn("FileTransactionContext not existed: " + fileTransactionId);
                return;
            }

            ChannelHandlerContext ctx = fileCtx.getCtx();
            MetaData metaData = msg.getChunkedMetaData().getMetaData();
            if (metaData == null)
                throw new BizException("metaData is null: " + fileTransactionId);

            if (msg.getChunkIndex() == 0) {
                // write response header
                String fileName = FileServerHttpResponseHelper.encodedDownloadFileName(fileCtx.getUserAgent(), metaData.getFileName());
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(metaData.getFileTotalSize()));
                response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, FileServerHttpResponseHelper.getContentType(metaData.getFileName()));
                response.headers().set("Content-disposition", "attachment;filename=" + fileName + ";filename*=UTF-8" + fileName);
                if (fileCtx.isKeepAlive()) {
                    response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                }

                ctx.write(response);
            }

            // Chunked write response body
            byte[] chunkedFileData = msg.getChunkedMetaData().getChunkedData();
            InputStream inputStream = new ByteArrayInputStream(chunkedFileData);
            ChunkedStream chunkedStream = new ChunkedStream(inputStream, chunkedFileData.length);
            ChannelFuture sendFileFuture;

            sendFileFuture = ctx.writeAndFlush(chunkedStream, ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {

                }

                public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                    try {
                        inputStream.close();
                        chunkedStream.close();
                    } catch (Exception e) {
                        log.warn("close inputStream or chunkedStream error!", e);
                    }

                    if (msg.getChunkIndex() == msg.getChunkCount() - 1) {
                        // 最后1个分片完成
                        FileTransactionContextManager.getInstance().removeContext(tid);
                        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        log.info("download process done: " + tid);
                    }
                }
            });

            if (msg.getChunkIndex() < msg.getChunkCount() - 1) {
                // 后续分片下载
                DownloadMsg downloadMsg = DownloadMsg.builder()
                        .fileTransactionId(fileTransactionId)
                        .metaDataIndex(msg.getMetaDataIndex())
                        .fileTotalSize(metaData.getFileTotalSize())
                        .chunkIndex(msg.getChunkIndex() + 1)
                        .build();
                onDownloadRequestReceived(downloadMsg);
            }
        } catch (Exception ex) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
        }
    }
}
