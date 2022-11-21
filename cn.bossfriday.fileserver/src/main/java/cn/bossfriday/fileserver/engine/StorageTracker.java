package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.fileserver.FileServerUtils;
import cn.bossfriday.fileserver.actors.model.*;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.model.MetaData;
import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

/**
 * StorageTracker
 *
 * @author chenx
 */
@Slf4j
public class StorageTracker {

    private static volatile StorageTracker instance = null;

    @Getter
    private ActorRef trackerActor;

    private StorageTracker() {
        try {
            this.trackerActor = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf(ACTOR_FS_TRACKER);
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
     *
     * @param msg
     */
    public void onPartialUploadDataReceived(WriteTmpFileMsg msg) {
        // 按fileTransactionId路由
        RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(msg.getFileTransactionId(), ACTOR_FS_TMP_FILE, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.trackerActor);
    }

    /**
     * 临时文件写入结果
     *
     * @param msg
     */
    public void onWriteTmpFileResultReceived(WriteTmpFileResult msg) {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerUtils.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());

            return;
        }

        // 零时文件写入整体完成
        if (msg.isFullDone()) {
            // 强制路由：同一个fileTransaction要求在同一个集群节点处理
            RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(msg.getClusterNodeName(), ACTOR_FS_UPLOAD, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.trackerActor);

            return;
        }

        // 当前Range分片写入完成（发送204 NoContent应答）
        String rangeHeaderValue = msg.getRange().getRangeResponseHeaderValue(msg.getFileTotalSize());
        FileServerUtils.sendRangeUploadNoContentResponse(msg.getFileTransactionId(), rangeHeaderValue);
    }

    /**
     * 上传结果
     *
     * @param msg
     */
    public void onUploadResultReceived(FileUploadResult msg) throws IOException {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerUtils.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());

            return;
        }

        String fileTransactionId = msg.getFileTransactionId();
        MetaDataIndex metaDataIndex = msg.getMetaDataIndex();
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(metaDataIndex.getStoreEngineVersion());
        String path = metaDataHandler.downloadUrlEncode(metaDataIndex);
        String uploadResponseBody = "{\"rc_url\":{\"path\":\"" + path + "\",\"type\":0}}";
        FileServerUtils.sendResponse(fileTransactionId, HttpResponseStatus.OK, String.valueOf(HttpHeaderValues.APPLICATION_JSON), uploadResponseBody, false);
        log.info(fileTransactionId + " upload done:" + uploadResponseBody);
    }

    /**
     * 下载请求
     *
     * @param msg
     */
    public void onDownloadRequestReceived(FileDownloadMsg msg) {
        String fileTransactionId = "";
        try {
            // 强制路由：优先从主节点下载
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex index = msg.getMetaDataIndex();
            RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(index.getClusterNode(), ACTOR_FS_DOWNLOAD, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.trackerActor);
        } catch (Exception ex) {
            log.error("onDownloadRequestReceived() process error: " + fileTransactionId, ex);
            FileServerUtils.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * 下载结果
     *
     * @param msg
     */
    public void onDownloadResult(FileDownloadResult msg) {
        String fileTransactionId = "";
        try {
            final String tid = fileTransactionId = msg.getFileTransactionId();
            if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
                FileServerUtils.sendResponse(msg.getFileTransactionId(), msg.getResult().getStatus(), msg.getResult().getMsg());

                return;
            }

            FileTransactionContext fileCtx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
            if (fileCtx == null) {
                log.warn("FileTransactionContext not existed: " + fileTransactionId);

                return;
            }

            ChannelHandlerContext ctx = fileCtx.getCtx();
            MetaData metaData = msg.getMetaData();
            if (metaData == null) {
                throw new BizException("metaData is null: " + fileTransactionId);
            }

            if (msg.getChunkIndex() == 0) {
                // write response header
                String fileName = FileServerUtils.encodedDownloadFileName(fileCtx.getUserAgent(), metaData.getFileName());
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(metaData.getFileTotalSize()));
                response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, FileServerUtils.getContentType(metaData.getFileName()));
                response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment;filename=" + fileName + ";filename*=UTF-8" + fileName);
                if (fileCtx.isKeepAlive()) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
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
                @Override
                public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) {
                    // just do nothing
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) {
                    try {
                        inputStream.close();
                        chunkedStream.close();
                    } catch (Exception e) {
                        log.warn("close inputStream or chunkedStream error!", e);
                    } finally {
                        if (msg.getChunkIndex() == msg.getChunkCount() - 1) {
                            // 最后1个分片完成
                            FileTransactionContextManager.getInstance().unregisterContext(tid);
                            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                            log.info("download process done: " + tid);
                        }
                    }
                }
            });

            if (msg.getChunkIndex() < msg.getChunkCount() - 1) {
                // 后续分片下载
                FileDownloadMsg fileDownloadMsg = FileDownloadMsg.builder()
                        .fileTransactionId(fileTransactionId)
                        .metaDataIndex(msg.getMetaDataIndex())
                        .metaData(metaData)
                        .chunkIndex(msg.getChunkIndex() + 1)
                        .build();
                this.onDownloadRequestReceived(fileDownloadMsg);
            }
        } catch (Exception ex) {
            FileServerUtils.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
        }
    }

    /**
     * 上传意外中断删除ing临时文件
     *
     * @param msg
     */
    public void onDeleteTmpFileMsg(DeleteTmpFileMsg msg) {
        RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(msg.getFileTransactionId(), ACTOR_FS_DEL_TMP_FILE, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.trackerActor);
    }

    /**
     * 文件删除请求
     *
     * @param msg
     */
    public void onFileDeleteMsg(FileDeleteMsg msg) {
        String fileTransactionId = "";
        try {
            // 强制路由：在主节点上进行删除（如果将来实现了高可用，则通过主从同步机制进行副本文件的删除）
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex index = msg.getMetaDataIndex();
            RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(index.getClusterNode(), ACTOR_FS_DELETE, msg);
            ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.trackerActor);
        } catch (Exception ex) {
            log.error("onFileDeleteMsg() process error: " + fileTransactionId, ex);
            FileServerUtils.sendResponse(fileTransactionId, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * 文件删除结果
     *
     * @param msg
     */
    public void onFileDeleteResultMsg(FileDeleteResult msg) {
        FileServerUtils.sendResponse(msg.getFileTransactionId(), msg.getResult().getStatus(), msg.getResult().getMsg());
    }
}
