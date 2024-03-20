package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.message.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * MessageInputStream
 *
 * @author chenx
 */
public class MessageInputStream implements Closeable {

    private InputStream in;

    public MessageInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * readMessage
     *
     * @return
     * @throws IOException
     */
    public MqttMessage readMessage() throws IOException {
        byte flags = (byte) this.in.read();
        MqttMessageHeader header = new MqttMessageHeader(flags);
        MqttMessage msg;
        switch (header.getType()) {
            case CONNACK:
                msg = new ConnAckMessage(header);
                break;
            case PUBLISH:
                msg = new PublishMessage(header);
                break;
            case PUBACK:
                msg = new PubAckMessage(header);
                break;
            case QUERY:
                msg = new QueryMessage(header);
                break;
            case QUERYACK:
                msg = new QueryAckMessage(header);
                break;
            case QUERYCON:
                msg = new QueryConMessage(header);
                break;
            case PINGRESP:
                msg = new PingRespMessage(header);
                break;
            case CONNECT:
                msg = new ConnectMessage(header);
                break;
            case PINGREQ:
                msg = new PingReqMessage(header);
                break;
            case DISCONNECT:
                msg = new DisconnectMessage(header);
                break;
            default:
                throw new UnsupportedOperationException("No support for deserializing " + header.getType() + " messages");
        }

        this.in.read();
        msg.read(this.in);

        return msg;
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
