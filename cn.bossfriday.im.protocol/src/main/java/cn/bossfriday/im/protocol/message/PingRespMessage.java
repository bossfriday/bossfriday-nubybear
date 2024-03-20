package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.enums.MqttMessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * PingRespMessage
 *
 * @author chenx
 */
public class PingRespMessage extends MqttMessage {

    public PingRespMessage(MqttMessageHeader header) {
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

    public PingRespMessage() {
        super(MqttMessageType.PINGRESP);
    }
}
