package cn.bossfriday.common.rpc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        byte[] data = RpcMessageCodec.serialize(msg);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
