package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.entity.ChunkedMetaData;
import cn.bossfriday.fileserver.engine.entity.MetaData;
import cn.bossfriday.fileserver.rpc.module.DownloadMsg;
import cn.bossfriday.fileserver.rpc.module.DownloadResult;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_DOWNLOAD;
import static cn.bossfriday.fileserver.common.FileServerConst.DOWNLOAD_CHUNK_SIZE;

@Slf4j
@ActorRoute(methods = ACTOR_FS_DOWNLOAD, poolName = ACTOR_FS_DOWNLOAD + "_Pool")
public class DownloadActor extends TypedActor<DownloadMsg> {

    /**
     * todo:从tracker —> DownloadActor 确保是同一个线程。
     *
     * @param msg
     * @throws Exception
     */
    @Override
    public void onMessageReceived(DownloadMsg msg) throws Exception {
        String fileTransactionId = "";
        DownloadResult result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            MetaData metaData = StorageEngine.getInstance().getMetaData(msg.getMetaDataIndex());
            long fileTotalSize = metaData.getFileTotalSize();
            long chunkIndex = msg.getChunkIndex();
            int chunkSize = DOWNLOAD_CHUNK_SIZE;
            long chunkCount = fileTotalSize % chunkSize == 0 ? (long) (fileTotalSize / chunkSize) : (long) (fileTotalSize / chunkSize + 1);
            long position = chunkIndex * chunkSize;
            int length = chunkSize;
            if (chunkIndex + 1 >= chunkCount) {
                int x = (int) (chunkCount * DOWNLOAD_CHUNK_SIZE - fileTotalSize);
                if (x < 0)
                    throw new BizException("invalid chunkCount: (chunkCount * chunkSize - fileTotalSize) < 0");

                length = DOWNLOAD_CHUNK_SIZE - x;
            }

            ChunkedMetaData chunkedMetaData = StorageEngine.getInstance().chunkedDownload(msg.getMetaDataIndex(), position, length);

            result = DownloadResult.builder()
                    .fileTransactionId(fileTransactionId)
                    .result(OperationResult.OK)
                    .metaDataIndex(msg.getMetaDataIndex())
                    .chunkIndex(msg.getChunkIndex())
                    .chunkCount(chunkCount)
                    .chunkedMetaData(chunkedMetaData)
                    .build();
        } catch (Exception ex) {
            log.error("DownloadActor process error: " + fileTransactionId, ex);
            result = new DownloadResult(msg.getFileTransactionId(), OperationResult.SystemError);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
            msg = null;
        }
    }
}
