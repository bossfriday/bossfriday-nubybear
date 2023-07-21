package cn.bossfriday.common.test.rpc;


import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;

public class Actor2 extends BaseUntypedActor {
    @Override
    public void onReceive(Object msg) {
        try {
            if (msg instanceof Foo) {
                this.getSender().tell(this.process((Foo) msg), ActorRef.noSender());
                return;
            }

        } finally {
            msg = null;
        }
    }

    private FooResult process(Foo foo) {
        // just do something
        return FooResult.builder().code(200).msg("ok").request(foo).build();
    }
}
