package cn.bossfriday.mocks.rpc.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.mocks.rpc.modules.Foo;
import cn.bossfriday.mocks.rpc.modules.FooResult;

@ActorRoute(methods = "fooServer")
public class FooServerActor extends TypedActor<Foo> {

    @Override
    public void onMessageReceived(Foo msg) throws Exception {
        this.getSender().tell(FooResultFactory.getFooResult(), ActorRef.noSender());
    }

    static class FooResultFactory {
        private static FooResult fooResult;

        static {
            fooResult = FooResult.builder().code(200).msg("ok").build();
        }

        public static FooResult getFooResult() {
            return fooResult;
        }
    }
}
