package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.UntypedActor;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_HTTP_FILE_SERVER;

@ActorRoute(methods = ACTOR_HTTP_FILE_SERVER)
public class HttpFileServerActor extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {

    }
}
