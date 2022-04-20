package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.http.FileServerHttpResponseHelper;
import cn.bossfriday.fileserver.rpc.module.UploadResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

/**
 * 文件服务调度Actor
 */
@Slf4j
@ActorRoute(methods = ACTOR_FS_TRACKER)
public class TrackerActor extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        try {
            if (msg instanceof WriteTmpFileResult) {
                WriteTmpFileResult result = (WriteTmpFileResult) msg;
                onWriteTmpFileResultReceived(result);
                return;
            }

            if (msg instanceof UploadResult) {
                UploadResult result = (UploadResult) msg;
                onUploadResultReceived(result);
                return;
            }
        } catch (Exception ex) {
            log.error("TrackerActor process error!", ex);
        }
    }

    /**
     * 上传完成
     * @param msg
     */
    private void onUploadResultReceived(UploadResult msg) {

    }

    /**
     * 临时文件处理完成
     */
    private void onWriteTmpFileResultReceived(WriteTmpFileResult msg) throws Exception {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());

            return;
        }

        // 强制路由：同一个fileTransaction要求在同一个集群节点处理
        RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(msg.getClusterNodeName(), ACTOR_FS_UPLOAD, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, this.getSelf());
    }
}
