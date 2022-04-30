package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_TMP_FILE;

@Slf4j
@ActorRoute(methods = ACTOR_FS_TMP_FILE, poolName = ACTOR_FS_TMP_FILE + "_Pool")
public class TmpFileActor extends TypedActor<WriteTmpFileMsg> {
    @Override
    public void onMessageReceived(WriteTmpFileMsg msg) throws Exception {
        WriteTmpFileResult result = null;
        try {
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            result = handler.write(msg);
        } catch (Exception ex) {
            log.error("WriteTmpFileActor process error!", ex);
            result = new WriteTmpFileResult(msg.getFileTransactionId(), OperationResult.SystemError);
        } finally {
            if (result != null) {
                this.getSender().tell(result, ActorRef.noSender());
            }

            msg = null;
        }
    }
}
