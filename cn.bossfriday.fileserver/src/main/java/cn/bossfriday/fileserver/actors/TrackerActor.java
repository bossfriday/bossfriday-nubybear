package cn.bossfriday.fileserver.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.fileserver.actors.model.FileDownloadResult;
import cn.bossfriday.fileserver.actors.model.FileUploadResult;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileResult;
import cn.bossfriday.fileserver.engine.StorageTracker;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_TRACKER;

/**
 * TrackerActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_TRACKER, poolName = ACTOR_FS_TRACKER + "_Pool")
public class TrackerActor extends BaseUntypedActor {

    @Override
    public void onReceive(Object msg) {
        try {
            if (msg instanceof WriteTmpFileResult) {
                StorageTracker.getInstance().onWriteTmpFileResultReceived((WriteTmpFileResult) msg);
                return;
            }

            if (msg instanceof FileUploadResult) {
                StorageTracker.getInstance().onUploadResultReceived((FileUploadResult) msg);
                return;
            }

            if (msg instanceof FileDownloadResult) {
                StorageTracker.getInstance().onDownloadResult((FileDownloadResult) msg);
                return;
            }
        } catch (Exception ex) {
            log.error("TrackerActor process error!", ex);
        }
    }
}
