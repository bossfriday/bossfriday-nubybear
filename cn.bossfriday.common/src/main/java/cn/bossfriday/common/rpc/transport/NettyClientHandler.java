package cn.bossfriday.common.rpc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private NettyClient client;
    public NettyClientHandler(NettyClient client){
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                client.connect();
            }
        }, 1L, TimeUnit.SECONDS);

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
