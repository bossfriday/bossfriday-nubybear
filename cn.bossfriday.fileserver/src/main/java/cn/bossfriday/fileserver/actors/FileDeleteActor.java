package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.im.common.enums.file.OperationResult;
import cn.bossfriday.im.common.message.file.FileDeleteInput;
import cn.bossfriday.im.common.message.file.FileDeleteOutput;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.constant.FileServerConstant.ACTOR_FS_DELETE;

/**
 * FileDeleteActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_DELETE)
public class FileDeleteActor extends BaseTypedActor<FileDeleteInput> {

    @Override
    public void onMessageReceived(FileDeleteInput msg) {
        String fileTransactionId = "";
        FileDeleteOutput result = null;
        try {
            fileTransactionId = msg.getFileTransactionId();
            IStorageHandler handler = StorageHandlerFactory.getStorageHandler(msg.getMetaDataIndex().getStoreEngineVersion());
            OperationResult operationResult = handler.delete(msg.getMetaDataIndex());
            result = new FileDeleteOutput(fileTransactionId, operationResult);
        } catch (Exception ex) {
            log.error("FileDeleteActor process error!", ex);
            result = new FileDeleteOutput(fileTransactionId, OperationResult.SYSTEM_ERROR);
        } finally {
            this.getSender().tell(result, ActorRef.noSender());
        }
    }
}
