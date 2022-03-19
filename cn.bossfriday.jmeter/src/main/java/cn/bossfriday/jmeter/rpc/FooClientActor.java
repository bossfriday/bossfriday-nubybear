package cn.bossfriday.jmeter.rpc;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.jmeter.rpc.modules.FooResult;

@ActorRoute(methods = "fooClient")
public class FooClientActor extends UntypedActor {

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof FooResult) {
            FooResult result = (FooResult) msg;
            long time = System.currentTimeMillis() - Long.valueOf(result.getMsg());
            System.out.println("process done, time:" + time);
            return;
        }
    }
}
