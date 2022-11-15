package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileMsg;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileResult;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageDispatcher;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_TMP_FILE;

/**
 * TmpFileActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_TMP_FILE, poolName = ACTOR_FS_TMP_FILE + "_Pool")
public class TmpFileActor extends BaseTypedActor<WriteTmpFileMsg> {

    @Override
    public void onMessageReceived(WriteTmpFileMsg msg) {
        ActorRef sender = this.getSender();
        /**
         * 保障FileTransactionId与其处理线程的一致性的原因为：临时文件的写盘机制是零拷贝+顺序写，
         */
        StorageDispatcher.getUploadThread(msg.getFileTransactionId()).execute(new Runnable() {
            @Override
            public void run() {
                TmpFileActor.this.process(sender, msg);
            }
        });
    }

    /**
     * process
     *
     * @param sender
     * @param msg
     */
    private void process(ActorRef sender, WriteTmpFileMsg msg) {
        WriteTmpFileResult result = null;
        try {
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            result = handler.write(msg);
        } catch (Exception ex) {
            log.error("WriteTmpFileActor process error!", ex);
            result = new WriteTmpFileResult(msg.getFileTransactionId(), OperationResult.SYSTEM_ERROR);
        } finally {
            try {
                if (result != null) {
                    sender.tell(result, ActorRef.noSender());
                }
            } catch (Exception e) {
                log.error("tell error!", e);
            }
        }
    }
}
