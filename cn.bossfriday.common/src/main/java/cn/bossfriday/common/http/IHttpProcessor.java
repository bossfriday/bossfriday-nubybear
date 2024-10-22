package cn.bossfriday.common.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * IHttpProcessor
 *
 * @author chenx
 */
public interface IHttpProcessor {

    /**
     * process
     *
     * @param ctx
     * @param httpRequest
     */
    void process(ChannelHandlerContext ctx, FullHttpRequest httpRequest);
}
