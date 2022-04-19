package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResp;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_WRITE_TMP_FILE;

@Slf4j
@ActorRoute(methods = ACTOR_WRITE_TMP_FILE)
public class WriteTmpFileActor extends TypedActor<WriteTmpFileMsg> {
    @Override
    public void onMessageReceived(WriteTmpFileMsg msg) throws Exception {
        try {
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            WriteTmpFileResp result = handler.write(msg);
            this.getSender().tell(result, ActorRef.noSender());
        } catch (Exception ex) {
            log.error("WriteTmpFileActor process error!", ex);
            WriteTmpFileResp result = new WriteTmpFileResp(msg.getFileTransactionId(), OperationResult.SystemError);
            this.getSender().tell(result, ActorRef.noSender());
        } finally {
            msg = null;
        }
    }
}
