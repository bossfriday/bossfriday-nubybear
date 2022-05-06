package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.rpc.module.DeleteTmpFileMsg;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_DEL_TMP_FILE;

@Slf4j
@ActorRoute(methods = ACTOR_FS_DEL_TMP_FILE)
public class DelTmpFileActor extends TypedActor<DeleteTmpFileMsg> {
    @Override
    public void onMessageReceived(DeleteTmpFileMsg msg) throws Exception {
        String fileTransactionId = "";
        try {
            fileTransactionId = msg.getFileTransactionId();
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            handler.deleteIngTmpFile(fileTransactionId);
        } catch (Exception ex) {
            log.error("DelTmpFileActor process error!", ex);
        }
    }
}
