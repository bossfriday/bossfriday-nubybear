package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.im.common.message.file.FileDeleteOutput;
import cn.bossfriday.im.common.message.file.FileDownloadOutput;
import cn.bossfriday.im.common.message.file.FileUploadOutput;
import cn.bossfriday.im.common.message.file.WriteTmpFileOutput;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.constant.FileServerConstant.ACTOR_FS_TRACKER;

/**
 * TrackerActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_TRACKER, poolName = ACTOR_FS_TRACKER + "_Pool")
public class TrackerActor extends BaseUntypedActor {

    @Override
    public void onMsgReceive(Object msg) {
        try {
            if (msg instanceof WriteTmpFileOutput) {
                StorageTracker.getInstance().onWriteTmpFileResultReceived((WriteTmpFileOutput) msg);
                return;
            }

            if (msg instanceof FileUploadOutput) {
                StorageTracker.getInstance().onUploadResultReceived((FileUploadOutput) msg);
                return;
            }

            if (msg instanceof FileDownloadOutput) {
                StorageTracker.getInstance().onDownloadResult((FileDownloadOutput) msg);
                return;
            }

            if (msg instanceof FileDeleteOutput) {
                StorageTracker.getInstance().onFileDeleteResultMsg((FileDeleteOutput) msg);
            }
        } catch (Exception ex) {
            log.error("TrackerActor process error!", ex);
        }
    }
}
