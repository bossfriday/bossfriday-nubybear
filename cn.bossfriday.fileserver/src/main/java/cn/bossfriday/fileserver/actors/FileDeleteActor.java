package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.actors.model.FileDeleteMsg;
import cn.bossfriday.fileserver.actors.model.FileDeleteResult;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_DELETE;

/**
 * FileDeleteActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_DELETE)
public class FileDeleteActor extends BaseTypedActor<FileDeleteMsg> {

    @Override
    public void onMessageReceived(FileDeleteMsg msg) {
        String fileTransactionId = "";
        FileDeleteResult result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            IStorageHandler handler = StorageHandlerFactory.getStorageHandler(msg.getMetaDataIndex().getStoreEngineVersion());
            OperationResult operationResult = handler.delete(msg.getMetaDataIndex());
            result = new FileDeleteResult(fileTransactionId, operationResult);
        } catch (Exception ex) {
            log.error("FileDeleteActor process error!", ex);
            result = new FileDeleteResult(fileTransactionId, OperationResult.SYSTEM_ERROR);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
        }
    }
}
