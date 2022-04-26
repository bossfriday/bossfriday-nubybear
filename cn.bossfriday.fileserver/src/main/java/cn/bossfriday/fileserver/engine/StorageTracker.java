package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.http.FileServerHttpResponseHelper;
import cn.bossfriday.fileserver.rpc.module.DownloadMsg;
import cn.bossfriday.fileserver.rpc.module.UploadResult;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

@Slf4j
public class StorageTracker {
    private volatile static StorageTracker instance = null;

    @Getter
    private ActorRef trackerActor;

    private StorageTracker() {
        try {
            trackerActor = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf(ACTOR_FS_TRACKER);
        } catch (Exception ex) {
            log.error("StorageTracker error!", ex);
        }
    }

    /**
     * getInstance
     */
    public static StorageTracker getInstance() {
        if (instance == null) {
            synchronized (StorageTracker.class) {
                if (instance == null) {
                    instance = new StorageTracker();
                }
            }
        }

        return instance;
    }

    /**
     * 临时文件写入请求
     */
    public void onPartialUploadDataReceived(WriteTmpFileMsg msg) throws Exception {
        // 按fileTransactionId路由
        RoutableBean routableBean = RoutableBeanFactory.buildKeyRouteBean(msg.getFileTransactionId(), ACTOR_FS_TMP_FILE, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
    }

    /**
     * 临时文件写入结果
     */
    public void onWriteTmpFileResultReceived(WriteTmpFileResult msg) throws Exception {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
            return;
        }

        // 强制路由：同一个fileTransaction要求在同一个集群节点处理
        RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(msg.getClusterNodeName(), ACTOR_FS_UPLOAD, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
    }

    /**
     * 上传结果
     */
    public void onUploadResultReceived(UploadResult msg) throws Exception {
        if (msg.getResult().getCode() != OperationResult.OK.getCode()) {
            FileServerHttpResponseHelper.sendResponse(msg.getFileTransactionId(), HttpResponseStatus.INTERNAL_SERVER_ERROR, msg.getResult().getMsg());
            return;
        }

        String fileTransactionId = msg.getFileTransactionId();;
        MetaDataIndex metaDataIndex = msg.getMetaDataIndex();
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(metaDataIndex.getStoreEngineVersion());
        String path = metaDataHandler.downloadUrlEncode(metaDataIndex);
        String uploadResponseBody = "{\"rc_url\":{\"path\":\"" + path + "\",\"type\":0}}";
        FileServerHttpResponseHelper.sendResponse(fileTransactionId, HttpResponseStatus.OK, HttpHeaders.Values.APPLICATION_JSON, uploadResponseBody, false);
        log.info(fileTransactionId + " upload done:" + uploadResponseBody);
    }

    /**
     * 下载请求
     */
    public void onDownloadRequestReceived(DownloadMsg msg) throws Exception {
        // 强制路由：从master节点下载（todo:如果master不用，则从slave节点下载）
        MetaDataIndex index = msg.getMetaDataIndex();
        RoutableBean routableBean = RoutableBeanFactory.buildForceRouteBean(index.getClusterNode(), ACTOR_FS_DOWNLOAD, msg);
        ClusterRouterFactory.getClusterRouter().routeMessage(routableBean, trackerActor);
    }
}
