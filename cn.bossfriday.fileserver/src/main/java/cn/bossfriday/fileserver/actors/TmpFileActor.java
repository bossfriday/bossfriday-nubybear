package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.fileserver.engine.StorageDispatcher;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.im.common.enums.file.OperationResult;
import cn.bossfriday.im.common.message.file.WriteTmpFileInput;
import cn.bossfriday.im.common.message.file.WriteTmpFileOutput;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.constant.FileServerConstant.ACTOR_FS_TMP_FILE;

/**
 * TmpFileActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_TMP_FILE, poolName = ACTOR_FS_TMP_FILE + "_Pool")
public class TmpFileActor extends BaseTypedActor<WriteTmpFileInput> {

    @Override
    public void onMessageReceived(WriteTmpFileInput msg) {
        ActorRef sender = this.getSender();
        /**
         * 保障FileTransactionId与其处理线程的一致性的原因为：临时文件的写盘机制是零拷贝+顺序写，
         */
        StorageDispatcher.getUploadThread(msg.getFileTransactionId()).execute(() -> TmpFileActor.this.process(sender, msg));
    }

    /**
     * process
     *
     * @param sender
     * @param msg
     */
    private void process(ActorRef sender, WriteTmpFileInput msg) {
        WriteTmpFileOutput result = null;
        try {
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            result = handler.write(msg);
        } catch (Exception ex) {
            log.error("WriteTmpFileActor process error!", ex);
            result = new WriteTmpFileOutput(msg.getFileTransactionId(), OperationResult.SYSTEM_ERROR);
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
