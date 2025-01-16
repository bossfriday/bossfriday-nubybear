package cn.bossfriday.im.user.actors;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.im.common.message.user.GetTokenInput;
import cn.bossfriday.im.common.message.user.GetTokenOutput;
import cn.bossfriday.im.common.rpc.BaseActor;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.common.constant.ImConstant.METHOD_USER_GET_TOKEN;

/**
 * GetTokenActor
 *
 * @author chenx
 */
@Slf4j
@ActorRoute(methods = METHOD_USER_GET_TOKEN)
public class GetTokenActor extends BaseActor<GetTokenInput> {

    @Override
    public void onMessageReceived(GetTokenInput msg) {
        GetTokenOutput output = new GetTokenOutput();
        output.setToken("token...");
        output.setUserId(msg.getUserId());
        this.getSender().tell(output, ActorRef.noSender());
    }
}
