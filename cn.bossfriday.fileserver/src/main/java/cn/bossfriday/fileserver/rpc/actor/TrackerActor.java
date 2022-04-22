package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.StorageTracker;
import cn.bossfriday.fileserver.http.FileServerHttpResponseHelper;
import cn.bossfriday.fileserver.rpc.module.UploadResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

@Slf4j
@ActorRoute(methods = ACTOR_FS_TRACKER)
public class TrackerActor extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        try {
            if (msg instanceof WriteTmpFileResult) {
                StorageTracker.getInstance().onWriteTmpFileResultReceived((WriteTmpFileResult) msg);
                return;
            }

            if (msg instanceof UploadResult) {
                StorageTracker.getInstance().onUploadResultReceived((UploadResult) msg);
                return;
            }
        } catch (Exception ex) {
            log.error("TrackerActor process error!", ex);
        }
    }
}
