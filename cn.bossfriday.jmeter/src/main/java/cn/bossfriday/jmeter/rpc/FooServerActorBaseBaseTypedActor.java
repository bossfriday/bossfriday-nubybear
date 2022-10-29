package cn.bossfriday.jmeter.rpc;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseTypedActor;
import cn.bossfriday.jmeter.rpc.modules.Foo;
import cn.bossfriday.jmeter.rpc.modules.FooResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import static cn.bossfriday.jmeter.common.Const.FOO_SERVER_METHOD_NAME;

@ActorRoute(methods = FOO_SERVER_METHOD_NAME)
public class FooServerActorBaseBaseTypedActor extends BaseTypedActor<Foo> {
    protected static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    public void onMessageReceived(Foo msg) {
        long time = System.currentTimeMillis() - msg.getTimestamp();
        FooResult result = new FooResult(200, "OK", time);
        this.getSender().tell(result, ActorRef.noSender());
    }
}
