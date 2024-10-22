package cn.bossfriday.im.api.processor.user;

import cn.bossfriday.common.http.IHttpProcessor;
import cn.bossfriday.common.register.HttpApiRoute;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.im.api.common.ApiConstant.API_ROUTE_KEY_USER_GET_TOKEN;

/**
 * GetTokenProcessor
 *
 * @author chenx
 */
@Slf4j
@HttpApiRoute(apiRouteKey = API_ROUTE_KEY_USER_GET_TOKEN)
public class GetTokenProcessor implements IHttpProcessor {

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

    }
}
