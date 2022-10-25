package cn.bossfriday.common.rpc.transport;

import cn.bossfriday.common.rpc.interfaces.IMsgHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * NettyServerHandler
 *
 * @author chenx
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    
    private IMsgHandler handler;

    public NettyServerHandler(IMsgHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        this.handler.msgHandle(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
