package cn.bossfriday.jmeter.rpc;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseTypedActorBase;
import cn.bossfriday.jmeter.rpc.modules.FooResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import static cn.bossfriday.jmeter.common.Const.FOO_METHOD_NAME;

@ActorRoute(methods = FOO_METHOD_NAME)
public class FooActorBaseBase extends BaseTypedActorBase<FooResult> {

    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    public void onMessageReceived(FooResult msg) {
        try {
            FooServerSampleLogWriter.writeSampleResultLog(System.currentTimeMillis(),
                    msg.getTime(),
                    "actorRpc-FooResult",
                    "200",
                    "ok",
                    Thread.currentThread().getName(),
                    true);
        } catch (Exception ex) {
            log.error("FooActor.onMessageReceived() error!", ex);
        }
    }
}
