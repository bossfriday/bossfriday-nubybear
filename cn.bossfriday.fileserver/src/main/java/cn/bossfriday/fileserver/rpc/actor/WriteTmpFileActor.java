package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_WRITE_TMP_FILE;

@Slf4j
@ActorRoute(methods = ACTOR_WRITE_TMP_FILE)
public class WriteTmpFileActor extends TypedActor<WriteTmpFileMsg> {
    @Override
    public void onMessageReceived(WriteTmpFileMsg msg) throws Exception {

    }

    @Override
    public void onFailed(Throwable cause) {
        super.onFailed(cause);
        log.error("WriteTmpFileActor failed.", cause);
    }
}
