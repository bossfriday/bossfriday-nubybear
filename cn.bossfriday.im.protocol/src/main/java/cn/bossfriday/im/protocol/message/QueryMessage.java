package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;
import cn.bossfriday.im.protocol.enums.MqttMessageType;

import java.io.*;

import static cn.bossfriday.im.protocol.core.MqttException.READ_DATA_UNEXPECTED_EXCEPTION;

/**
 * QueryMessage
 *
 * @author chenx
 */
public class QueryMessage extends RetryableMqttMessage {

    private String topic;
    private byte[] data;
    private String targetId;
    private long signature;

    public QueryMessage(String topic, byte[] data, String targetId) {
        super(MqttMessageType.QUERY);
        this.topic = topic;
        this.targetId = targetId;
        this.data = data;
        this.signature = 0xffL;
    }

    public QueryMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    protected int getMessageLength() {
        int length = 8;
        length += this.toUtfBytes(this.topic).length;
        length += this.toUtfBytes(this.targetId).length;
        length += 2;
        length += this.data.length;

        return length;
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeLong(this.signature);
        dos.writeUTF(this.topic);
        dos.writeUTF(this.targetId);
        dos.flush();
        super.writeMessage(out);
        dos.write(this.data);
        dos.flush();
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        int pos = 0;
        DataInputStream dis = new DataInputStream(in);
        this.signature = dis.readLong();
        pos += 8;

        this.topic = dis.readUTF();
        pos += this.toUtfBytes(this.topic).length;

        this.targetId = dis.readUTF();
        pos += this.toUtfBytes(this.targetId).length;

        super.readMessage(in, msgLength);
        pos += 2;

        int dataSize = msgLength - pos;
        this.data = new byte[dataSize];
        if (dis.read(this.data) != dataSize) {
            throw READ_DATA_UNEXPECTED_EXCEPTION;
        }
    }

    public String getTopic() {
        return this.topic;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getTargetId() {
        return this.targetId;
    }

    public String getDataAsString() {
        return new String(this.data);
    }
}
