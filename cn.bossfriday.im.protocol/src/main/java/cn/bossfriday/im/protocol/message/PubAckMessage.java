package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;
import static cn.bossfriday.im.protocol.core.MqttConstant.STATUS_OK;

/**
 * PubAckMessage
 * <p>
 * 2字节：msgSequenceId
 * 4字节：msgSecond
 * 2字节：resultCode
 * 2字节：msgMilliSecond
 * 11字节：msgUid
 *
 * @author chenx
 */
public class PubAckMessage extends RetryableMqttMessage {

    private int status;
    private int date;
    private short milliSecond;
    private String msgUid;

    public PubAckMessage(int messageId) {
        super(MqttMessageType.PUBACK);
        this.setMessageSequence(messageId);
    }

    public PubAckMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    protected int determineLength() {
        return FIX_HEADER_LENGTH;
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        super.readMessage(in, msgLength);
        DataInputStream dis = new DataInputStream(in);
        this.date = dis.readInt();
        this.status = dis.readShort();
        this.milliSecond = dis.readShort();

        if (this.status == STATUS_OK) {
            this.msgUid = dis.readUTF();
        }
    }

    public int getStatus() {
        return this.status;
    }

    public int getDate() {
        return this.date;
    }

    public short getMilliSecond() {
        return this.milliSecond;
    }

    public String getMsgUid() {
        return this.msgUid;
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("PubAckMessage don't use the DUP flag.");
    }

    @Override
    public void setRetained(boolean retain) {
        throw new MqttException("PubAckMessage don't use the RETAIN flag.");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("PubAckMessage don't use the QoS flags.");
    }
}
