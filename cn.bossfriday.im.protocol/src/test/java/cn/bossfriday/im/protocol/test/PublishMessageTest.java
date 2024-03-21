package cn.bossfriday.im.protocol.test;

import cn.bossfriday.im.protocol.codec.MqttMessageDecoder;
import cn.bossfriday.im.protocol.codec.MqttMessageEncoder;
import cn.bossfriday.im.protocol.codec.MqttMessageInputStream;
import cn.bossfriday.im.protocol.message.PublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * PublishMessageTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishMessageTest {

    @Mock
    private ChannelHandlerContext mockCtx;

    @Before
    public void mockInit() {

    }

    /**
     * 消息读写测试
     */
    @Test
    public void readWriteMessageTest() throws IOException {
        String topic = "topic-1";
        String targetId = "targetId-1";
        byte[] data = "中文abc1234!@#$".getBytes(StandardCharsets.UTF_8);
        boolean isServer = true;
        PublishMessage pubMsg1 = new PublishMessage(topic, data, targetId, isServer);
        pubMsg1.setMessageSequence(123);

        // MqttMessage -> bytes
        byte[] msgData = pubMsg1.toBytes();

        // bytes -> MqttMessage
        PublishMessage pubMsg2 = (PublishMessage) MqttMessageInputStream.readMessage(new ByteArrayInputStream(msgData), isServer);
        System.out.println("topic: " + pubMsg2.getTopic());
        System.out.println("targetId: " + pubMsg2.getTargetId());
        System.out.println("dataString: " + new String(pubMsg2.getData(), StandardCharsets.UTF_8));

        Assert.assertEquals(pubMsg2.getTopic(), topic);
        Assert.assertEquals(pubMsg2.getTargetId(), targetId);
        Assert.assertEquals(new String(pubMsg2.getData(), StandardCharsets.UTF_8), new String(data, StandardCharsets.UTF_8));
    }

    /**
     * 消息编解码器测试
     */
    @Test
    public void messageCodecTest() throws IOException {
        ByteBuf buf = Unpooled.buffer();

        String topic = "topic-1";
        String targetId = "targetId-1";
        byte[] data = "中文abc1234!@#$".getBytes(StandardCharsets.UTF_8);
        boolean isServer = true;
        PublishMessage pubMsg1 = new PublishMessage(topic, data, targetId, isServer);
        pubMsg1.setMessageSequence(123);

        // 编码
        MqttMessageEncoder msgEncoder = new MqttMessageEncoder();
        msgEncoder.encode(pubMsg1, buf);

        // 解码
        MqttMessageDecoder msgDecoder = new MqttMessageDecoder(6000L, targetId, isServer);
        PublishMessage pubMsg2 = (PublishMessage) msgDecoder.decode(this.mockCtx, buf);

        System.out.println("topic: " + pubMsg2.getTopic());
        System.out.println("targetId: " + pubMsg2.getTargetId());
        System.out.println("dataString: " + new String(pubMsg2.getData(), StandardCharsets.UTF_8));
        Assert.assertEquals(pubMsg2.getTopic(), topic);
        Assert.assertEquals(pubMsg2.getTargetId(), targetId);
        Assert.assertEquals(new String(pubMsg2.getData(), StandardCharsets.UTF_8), new String(data, StandardCharsets.UTF_8));
    }
}

