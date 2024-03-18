package cn.bossfriday.im.protocol.core;

import cn.bossfriday.im.protocol.enums.MqttMessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;

/**
 * RetryableMqttMessage
 *
 * @author chenx
 */
public abstract class RetryableMqttMessage extends MqttMessage {

    /**
     * 在MQTT协议栈中，消息序号（messageSequence）通常用于标识消息的顺序和唯一性。这里的视线中消息序号是一个2字节的字段（最大可以表达无符号整型65535），用于发布（publish）消息和发布确认（publishAck）消息的对齐。
     * 具体而言，消息序号在协议栈中的用途包括：
     * 1、消息排序和唯一性：每个消息都有一个唯一的序号，用于在通信中标识消息的顺序和确保消息的唯一性。这在处理分片消息或者确保消息到达的顺序十分重要。
     * 2、重传机制：当消息需要重传时，消息序号可以用于标识需要重传的消息。例如，在发布消息（publish）时，如果没有收到确认，发送方可能会重新发送该消息，而消息序号可以确保接收方能够识别重传的消息。
     * 3、质量等级为1和2的消息传递：在MQTT中，质量等级（QoS）为1或2的消息需要确认。消息序号可以用于匹配发布消息和发布确认消息，从而实现消息的可靠传输。
     * 消息序号在MQTT协议栈中扮演了重要的角色，用于确保消息的顺序性、唯一性和可靠传输。
     */
    private int messageSequence;

    protected RetryableMqttMessage(MqttMessageHeader header) {
        super(header);
    }

    protected RetryableMqttMessage(MqttMessageType mqttMessageType) {
        super(mqttMessageType);
    }

    @Override
    protected int determineLength() {
        return FIX_HEADER_LENGTH;
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        int id = this.getMessageSequence();
        int lsb = id & 0xFF;
        int msb = (id & 0xFF00) >> 8;
        out.write(msb);
        out.write(lsb);
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        int msgId = in.read() * 0x100 + in.read();
        this.setMessageSequence(msgId);
    }

    public void setMessageSequence(int messageSequence) {
        this.messageSequence = messageSequence;
    }

    public int getMessageSequence() {
        return this.messageSequence;
    }
}