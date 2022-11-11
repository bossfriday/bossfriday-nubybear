package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.actors.model.FileUploadResult;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileResult;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_UPLOAD;

/**
 * FileUploadActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_UPLOAD, poolName = ACTOR_FS_UPLOAD + "_Pool")
public class FileUploadActor extends BaseTypedActor<WriteTmpFileResult> {

    @Override
    public void onMessageReceived(WriteTmpFileResult msg) {

        String fileTransactionId = "";
        FileUploadResult result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex metaDataIndex = StorageEngine.getInstance().upload(msg);
            if (metaDataIndex == null) {
                throw new BizException("MetaDataIndex is null: " + fileTransactionId);
            }

            result = new FileUploadResult(msg.getFileTransactionId(), OperationResult.OK, metaDataIndex);
        } catch (Exception ex) {
            log.error("UploadActor process error: " + fileTransactionId, ex);
            result = new FileUploadResult(msg.getFileTransactionId(), OperationResult.SYSTEM_ERROR);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
            msg = null;
        }
    }
}
