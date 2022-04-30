package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.rpc.module.UploadResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_UPLOAD;

@Slf4j
@ActorRoute(methods = ACTOR_FS_UPLOAD, poolName = ACTOR_FS_UPLOAD + "_Pool")
public class UploadActor extends TypedActor<WriteTmpFileResult> {
    @Override
    public void onMessageReceived(WriteTmpFileResult msg) throws Exception {

        String fileTransactionId = "";
        UploadResult result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            MetaDataIndex metaDataIndex = StorageEngine.getInstance().upload(msg);
            if (metaDataIndex == null) {
                throw new BizException("MetaDataIndex is null: " + fileTransactionId);
            }

            result = new UploadResult(msg.getFileTransactionId(), OperationResult.OK, metaDataIndex);
        } catch (Exception ex) {
            log.error("UploadActor process error: " + fileTransactionId, ex);
            result = new UploadResult(msg.getFileTransactionId(), OperationResult.SystemError);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
            msg = null;
        }
    }
}
