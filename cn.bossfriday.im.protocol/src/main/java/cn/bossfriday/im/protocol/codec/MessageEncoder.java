package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * MessageEncoder
 *
 * @author chenx
 */
public class MessageEncoder extends MessageToByteEncoder<MqttMessage> {

    private int accessKeyType;

    public MessageEncoder(int keyType) {
        this.accessKeyType = keyType;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MqttMessage msg, ByteBuf out) throws Exception {
        if (Objects.isNull(msg)) {
            throw new MqttException("msg is null!");
        }

        if (Objects.isNull(out)) {
            throw new MqttException("outByteBuf is null!");
        }

        byte[] data = msg.toBytes();
        data = MessageObfuscator.obfuscateData(data, 2 + msg.getLength(), this.accessKeyType);
        out.writeBytes(data);
    }
}
