package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;
import cn.bossfriday.im.protocol.enums.MqttMessageType;

import java.io.*;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;
import static cn.bossfriday.im.protocol.core.MqttException.READ_DATA_UNEXPECTED_EXCEPTION;

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
    private boolean isServer;

    public PublishMessage(String topic, byte[] data, String targetId, boolean isServer) {
        super(MqttMessageType.PUBLISH);
        this.topic = topic;
        this.targetId = targetId;
        this.data = data;
        this.signature = 0xffL;
        this.isServer = isServer;
    }

    public PublishMessage(MqttMessageHeader header, boolean isServer) {
        super(header);
        this.isServer = isServer;
    }

    @Override
    protected int getMessageLength() {
        int length = FIX_HEADER_LENGTH + Long.BYTES;
        if (this.isServer) {
            length += Integer.BYTES;
        }

        length += this.toUtfBytes(this.topic).length;
        length += this.toUtfBytes(this.targetId).length;
        length += this.data.length;

        return length;
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeLong(this.signature);

        if (this.isServer) {
            this.date = (int) (System.currentTimeMillis() / 1000);
            dos.writeInt(this.date);
        }

        dos.writeUTF(this.topic);
        dos.writeUTF(this.targetId);
        dos.flush();
        super.writeMessage(out);
        dos.write(this.data);
        dos.flush();
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int pos = 0;

        this.signature = dis.readLong();
        pos += 8;

        this.date = dis.readInt();
        pos += 4;

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

    public int getDate() {
        return this.date;
    }
}
