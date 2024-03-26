package cn.bossfriday.im.protocol.core;

import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

import java.io.*;

/**
 * Message
 *
 * @author chenx
 */
public abstract class MqttMessage {

    private final MqttMessageHeader header;
    private byte headerCode;
    private int lengthSize = 0;

    protected MqttMessage(MqttMessageType mqttMessageType) {
        this.header = new MqttMessageHeader(mqttMessageType, false, QoS.AT_MOST_ONCE, false);
    }

    protected MqttMessage(MqttMessageHeader header) {
        this.header = header;
    }

    /**
     * getMessageLength
     *
     * @return
     */
    protected abstract int getMessageLength();

    /**
     * writeMessage
     *
     * @param out
     * @throws IOException
     */
    protected abstract void writeMessage(OutputStream out) throws IOException;

    /**
     * readMessage
     *
     * @param in
     * @param msgLength
     * @throws IOException
     */
    protected abstract void readMessage(InputStream in, int msgLength) throws IOException;

    /**
     * read
     *
     * @param in
     * @throws IOException
     */
    public final void read(InputStream in) throws IOException {
        int msgLength = this.readMsgLength(in);
        this.readMessage(in, msgLength);
    }

    /**
     * write
     * <p>
     * | 类型 | 位置 | 说明 |
     * |--------|--------|--------|
     * | 固定头 | 第1字节 | MsgType (消息类型：4 bits)；DUP (重传标记：1 bits)；QoS (质量等级：2 bits)；RETAIN (保留位：1 bits) |
     * | 固定头 | 第2字节 |  校验和（Checksum）：固定头第1字节与剩余长度进行按位异或 |
     * | 可变头 | 第3-J字节 | 剩余长度：最多3字节的变长Int |
     * | 可变头 | 第K-L字节 | signature（鉴权信息） 、topic（可以理解为信令名）、targetId（目标用户ID） |
     * | 荷载   | 第 M-N 字节 | 消息体Bytes |
     *
     * @param out
     * @throws IOException
     */
    public final void write(OutputStream out) throws IOException {
        this.headerCode = this.header.encode();
        out.write(this.headerCode);
        this.writeCheckSum(out);
        this.writeMsgLength(out);
        this.writeMessage(out);
    }

    /**
     * toBytes
     *
     * @return
     */
    public final byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.write(byteArrayOutputStream);
        } catch (IOException e) {
            throw new MqttException("Message.toBytes() error!");
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * toUtfBytes
     *
     * @param s
     * @return
     */
    public final byte[] toUtfBytes(String s) {
        if (s == null) {
            return new byte[0];
        }

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(byteOut)) {
            dos.writeUTF(s);
            dos.flush();

            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new MqttException("MessageObfuscator.writeString() error!");
        }
    }
    
    public final int getLengthSize() {
        return this.lengthSize;
    }

    public void setRetained(boolean retain) {
        this.header.setRetain(retain);
    }

    public boolean isRetained() {
        return this.header.isRetained();
    }

    public void setQos(QoS qos) {
        this.header.setQos(qos);
    }

    public QoS getQos() {
        return this.header.getQos();
    }

    public void setDup(boolean dup) {
        this.header.setDup(dup);
    }

    public boolean isDup() {
        return this.header.isDup();
    }

    public MqttMessageType getType() {
        return this.header.getMqttMessageType();
    }

    /**
     * readMsgLength
     */
    private int readMsgLength(InputStream in) throws IOException {
        int msgLength = 0;
        int multiplier = 1;
        int digit;
        do {
            digit = in.read();
            msgLength += (digit & 0x7f) * multiplier;
            multiplier *= 128;
        } while ((digit & 0x80) > 0);

        return msgLength;
    }

    /**
     * writeMsgLength
     * <p>
     * 每字节的最高表达是否还有后续字节，因此每自己实际用于表达数值的只有7位；
     * 这样，如果最大3字节的VarInt最大范围为：2的21次方减1 ≈ 2MB
     */
    private void writeMsgLength(OutputStream out) throws IOException {
        int val = this.getMessageLength();

        do {
            this.lengthSize++;
            byte b = (byte) (val & 0x7F);
            val >>= 7;
            if (val > 0) {
                b |= 0x80;
            }

            out.write(b);
        } while (val > 0);
    }

    /**
     * writeCheckSum
     */
    private void writeCheckSum(OutputStream out) throws IOException {
        int val = this.getMessageLength();
        int code = this.headerCode;

        do {
            byte b = (byte) (val & 0x7F);
            val >>= 7;
            if (val > 0) {
                b |= 0x80;
            }
            code = code ^ b;
        } while (val > 0);

        out.write(code);
    }
}
