package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.actors.module.FileDownloadMsg;
import cn.bossfriday.fileserver.actors.module.FileDownloadResult;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.entity.ChunkedMetaData;
import cn.bossfriday.fileserver.engine.entity.MetaData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.actors.module.FileDownloadMsg.FIRST_CHUNK_INDEX;
import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_DOWNLOAD;
import static cn.bossfriday.fileserver.common.FileServerConst.DOWNLOAD_CHUNK_SIZE;

/**
 * FileDownloadActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_DOWNLOAD, poolName = ACTOR_FS_DOWNLOAD + "_Pool")
public class FileDownloadActor extends BaseTypedActor<FileDownloadMsg> {

    @Override
    public void onMessageReceived(FileDownloadMsg msg) {
        String fileTransactionId = "";
        FileDownloadResult result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex metaDataIndex = msg.getMetaDataIndex();
            MetaData metaData = (msg.getChunkIndex() == FIRST_CHUNK_INDEX) ? StorageEngine.getInstance().getMetaData(metaDataIndex) : msg.getMetaData();

            long fileTotalSize = metaData.getFileTotalSize();
            long chunkIndex = msg.getChunkIndex();
            int chunkSize = DOWNLOAD_CHUNK_SIZE;
            long chunkCount = fileTotalSize % chunkSize == 0 ? (fileTotalSize / chunkSize) : (fileTotalSize / chunkSize + 1);
            long offset = chunkIndex * chunkSize;
            int limit = chunkSize;
            if (chunkIndex + 1 >= chunkCount) {
                int x = (int) (chunkCount * DOWNLOAD_CHUNK_SIZE - fileTotalSize);
                if (x < 0) {
                    throw new BizException("invalid chunkCount: (chunkCount * chunkSize - fileTotalSize) < 0");
                }

                limit = DOWNLOAD_CHUNK_SIZE - x;
            }

            ChunkedMetaData chunkedMetaData = StorageEngine.getInstance().chunkedDownload(metaDataIndex, metaData, offset, limit);
            result = FileDownloadResult.builder()
                    .fileTransactionId(fileTransactionId)
                    .result(OperationResult.OK)
                    .metaDataIndex(msg.getMetaDataIndex())
                    .metaData(metaData)
                    .chunkIndex(msg.getChunkIndex())
                    .chunkCount(chunkCount)
                    .chunkedMetaData(chunkedMetaData)
                    .build();
        } catch (Exception ex) {
            log.error("DownloadActor process error: " + fileTransactionId, ex);
            result = new FileDownloadResult(msg.getFileTransactionId(), OperationResult.SYSTEM_ERROR);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
        }
    }
}
