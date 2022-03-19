package cn.bossfriday.common.rpc.transport;

import cn.bossfriday.common.rpc.interfaces.IMsgHandler;
import cn.bossfriday.common.rpc.interfaces.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer implements IServer {
    private int port;
    private Channel channel = null;
    private final IMsgHandler msgHandler;

    public NettyServer(int port, IMsgHandler msgHandler) {
        this.port = port;
        this.msgHandler = msgHandler;
    }

    @Override
    public void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcDecoder());
                        ch.pipeline().addLast(new RpcEncoder());
                        ch.pipeline().addLast(new NettyServerHandler(msgHandler));
                    }

                });
        this.channel = b.bind(port).sync().channel();
    }

    @Override
    public void close() throws Exception {
        if(this.channel != null){
            this.channel.close();
            this.channel.closeFuture().sync();
        }
    }
}
