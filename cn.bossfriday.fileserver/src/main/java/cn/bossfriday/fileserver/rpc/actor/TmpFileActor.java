package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageDispatcher;
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
        ActorRef sender = this.getSender();
        /**
         * 为了保障临时文件顺序写盘，每个事务使用唯一线程。
         * 实际上多线程写临时文件的结果也是正确的（因为提前计算好了offset）
         * 这里主要目的为：对磁盘友好
         */
        StorageDispatcher.getUploadThread(msg.getFileTransactionId()).execute(new Runnable() {
            @Override
            public void run() {
                process(sender, msg);
            }
        });
    }

    private void process(ActorRef sender, WriteTmpFileMsg msg) {
        WriteTmpFileResult result = null;
        try {
            ITmpFileHandler handler = StorageHandlerFactory.getTmpFileHandler(msg.getStorageEngineVersion());
            result = handler.write(msg);
        } catch (Exception ex) {
            log.error("WriteTmpFileActor process error!", ex);
            result = new WriteTmpFileResult(msg.getFileTransactionId(), OperationResult.SystemError);
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
