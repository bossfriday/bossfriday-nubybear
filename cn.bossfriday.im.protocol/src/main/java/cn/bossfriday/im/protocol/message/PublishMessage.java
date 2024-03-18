package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;
import cn.bossfriday.im.protocol.enums.MqttMessageType;

import java.io.*;

/**
 * PublishMessage
 *
 * @author chenx
 */
public class PublishMessage extends RetryableMqttMessage {

    private String topic;
    private byte[] data;
    private String targetId;
    private long signature;
    private int date;

    public PublishMessage(String topic, byte[] data, String targetId) {
        super(MqttMessageType.PUBLISH);
        this.topic = topic;
        this.targetId = targetId;
        this.data = data;
        this.signature = 0xffL;
        this.date = (int) (System.currentTimeMillis() / 1000);
    }

    public PublishMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    protected int determineLength() {
        int length = 10;
        length += this.toUtfBytes(this.topic).length;
        length += this.toUtfBytes(this.targetId).length;
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
        int pos = 14;
        DataInputStream dis = new DataInputStream(in);
        this.signature = dis.readLong();
        this.date = dis.readInt();
        this.topic = dis.readUTF();
        this.targetId = dis.readUTF();
        pos += this.toUtfBytes(this.topic).length;
        pos += this.toUtfBytes(this.targetId).length;
        super.readMessage(in, msgLength);
        this.data = new byte[msgLength - pos];
        dis.read(this.data);
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

    public int getDate() {
        return this.date;
    }
}
