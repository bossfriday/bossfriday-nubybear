package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResp;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_HTTP_FILE_SERVER;

@Slf4j
@ActorRoute(methods = ACTOR_HTTP_FILE_SERVER)
public class HttpFileServerActor extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof WriteTmpFileResp) {
            WriteTmpFileResp result = (WriteTmpFileResp) msg;
            onWriteTmpFileRespReceived(result);
            return;
        }
    }

    private void onWriteTmpFileRespReceived(WriteTmpFileResp result) {

    }
}
