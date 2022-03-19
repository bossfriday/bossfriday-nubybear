package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.utils.UUIDUtil;

public class DeadLetterActorRef extends ActorRef {
    public static final ActorRef Instance = new DeadLetterActorRef();

    public DeadLetterActorRef() {
        super(Const.DEAD_LETTER_ACTOR_HOST, Const.DEAD_LETTER_ACTOR_PORT, UUIDUtil.getUUIDBytes(), (String) null, null);
    }

    @Override
    public void tell(Object message, ActorRef sender) {
        // it is empty
    }
}
