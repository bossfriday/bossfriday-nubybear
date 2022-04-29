package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.rpc.module.DownloadMsg;
import cn.bossfriday.fileserver.rpc.module.DownloadResult;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_DOWNLOAD;

@Slf4j
@ActorRoute(methods = ACTOR_FS_DOWNLOAD)
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
            byte[] chunkedFileData = StorageEngine.getInstance().chunkedDownload(fileTransactionId, msg.getMetaDataIndex(), msg.getChunkIndex());
            if(chunkedFileData == null)
                throw new BizException("chunkedFileData is null: " + fileTransactionId);

            result = DownloadResult.builder()
                    .fileTransactionId(fileTransactionId)
                    .result(OperationResult.OK)
                    .metaDataIndex(msg.getMetaDataIndex())
                    .chunkIndex(msg.getChunkIndex())
                    .chunkedFileData(chunkedFileData)
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
