package cn.bossfriday.jmeter.rpc;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.jmeter.rpc.modules.FooResult;

import static cn.bossfriday.jmeter.common.Const.FOO_METHOD_NAME;

@ActorRoute(methods = FOO_METHOD_NAME)
public class FooActor extends TypedActor<FooResult> {
    @Override
    public void onMessageReceived(FooResult msg) throws Exception {
        FooServerSampleLogWriter.writeSampleResultLog(System.currentTimeMillis(),
                msg.getTime(),
                "actorRpc-FooResult",
                "200",
                "ok",
                Thread.currentThread().getName(),
                true);
    }
}
