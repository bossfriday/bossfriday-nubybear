package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * MqttMessageEncoder
 *
 * @author chenx
 */
public class MqttMessageEncoder extends MessageToByteEncoder<MqttMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MqttMessage msg, ByteBuf out) {
        this.encode(msg, out);
    }

    /**
     * encode
     *
     * @param msg
     * @param out
     */
    public void encode(MqttMessage msg, ByteBuf out) {
        if (Objects.isNull(msg)) {
            throw new MqttException("msg is null!");
        }

        if (Objects.isNull(out)) {
            throw new MqttException("outByteBuf is null!");
        }

        byte[] data = msg.toBytes();
        data = MqttMessageObfuscator.obfuscateData(data, 2 + msg.getLengthSize());
        out.writeBytes(data);
    }
}

