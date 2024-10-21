package cn.bossfriday.im.api.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.BaseActor;
import cn.bossfriday.im.common.message.context.ActorContext;
import cn.bossfriday.im.common.message.user.GetTokenInput;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.ImConstant.ACTOR_POOL_NAME_API;
import static cn.bossfriday.im.common.ImConstant.METHOD_USER_GET_TOKEN;

/**
 * GetTokenOutputActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = METHOD_USER_GET_TOKEN, poolName = ACTOR_POOL_NAME_API)
public class GetTokenOutputActor extends BaseActor<GetTokenInput, ActorContext> {

    @Override
    public void onMessageReceived(GetTokenInput msg) {

    }
}
