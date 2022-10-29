package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.rpc.module.FileDownloadResult;
import cn.bossfriday.fileserver.rpc.module.FileUploadResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
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
