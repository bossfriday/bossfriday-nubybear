package cn.bossfriday.common.test.rpc;

import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.ActorMsgPayloadCodec;
import cn.bossfriday.common.rpc.interfaces.IActorMsgDecoder;
import cn.bossfriday.common.rpc.interfaces.IActorMsgEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class BarCluster {
    private static BarCluster instance;

    @Getter
    ActorSystem actorSystem;

    static {
        try {
            ActorSystem actorSystem = ActorSystem.create("BarCluster", new InetSocketAddress("127.0.0.1", 8090));
            actorSystem.setMsgEncoder(new IActorMsgEncoder() {
                @Override
                public byte[] encode(Object obj) {
                    return ActorMsgPayloadCodec.encode(obj);
                }
            });
            actorSystem.setMsgDecoder(new IActorMsgDecoder() {
                @Override
                public Object decode(byte[] bytes) {
                    return ActorMsgPayloadCodec.decode(bytes);
                }
            });

            actorSystem.registerActor("method1", 1, 2, Actor1.class);
            actorSystem.registerActor("method2", 1, 2, Actor2.class);

            instance = new BarCluster(actorSystem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BarCluster(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    /**
     * getInstance
     */
    public static BarCluster getInstance() {
        return instance;
    }

    /**
     * start
     */
    public void start() throws Exception {
        this.actorSystem.start();
    }
}
