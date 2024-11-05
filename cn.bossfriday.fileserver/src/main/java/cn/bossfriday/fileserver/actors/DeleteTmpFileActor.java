package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.actors.model.DeleteTmpFileMsg;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.constant.FileServerConstant.ACTOR_FS_DEL_TMP_FILE;

/**
 * DeleteTmpFileActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_DEL_TMP_FILE)
public class DeleteTmpFileActor extends BaseTypedActor<DeleteTmpFileMsg> {

    @Override
    public void onMessageReceived(DeleteTmpFileMsg msg) {
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
