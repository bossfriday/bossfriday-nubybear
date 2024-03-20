package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * PingReqMessage
 *
 * @author chenx
 */
public class PingReqMessage extends MqttMessage {

    public PingReqMessage() {
        super(MqttMessageType.PINGREQ);
    }

    public PingReqMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    protected int getMessageLength() {
        return 0;
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        // do nothing
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        // do nothing
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("PingReqMessage does not support the DUP flag");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("PingReqMessage does not support the QoS flag");
    }

    @Override
    public void setRetained(boolean retain) {
        throw new MqttException("PingReqMessage does not support the RETAIN flag");
    }
}
