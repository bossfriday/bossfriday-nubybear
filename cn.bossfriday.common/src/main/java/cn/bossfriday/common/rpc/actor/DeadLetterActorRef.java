package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.common.SystemConstant;
import cn.bossfriday.common.utils.UUIDUtil;

/**
 * DeadLetterActorRef
 *
 * @author chenx
 */
public class DeadLetterActorRef extends ActorRef {

    public static final ActorRef DEAD_LETTER_ACTOR_REF_INSTANCE = new DeadLetterActorRef();

    public DeadLetterActorRef() {
        super(SystemConstant.DEAD_LETTER_ACTOR_HOST, SystemConstant.DEAD_LETTER_ACTOR_PORT, UUIDUtil.getUUIDBytes(), (String) null, null);
    }

    @Override
    public void tell(Object message, ActorRef sender) {
        // it is empty
    }
}
