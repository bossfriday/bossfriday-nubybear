package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.message.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * MessageInputStream
 *
 * @author chenx
 */
public class MessageInputStream {

    private MessageInputStream() {
        // do nothing
    }

    /**
     * readMessage
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static MqttMessage readMessage(InputStream in, boolean isServer) throws IOException {
        MqttMessage msg = null;
        try {
            byte flags = (byte) in.read();
            in.read();
            MqttMessageHeader header = new MqttMessageHeader(flags);
            switch (header.getType()) {
                case CONNACK:
                    msg = new ConnAckMessage(header);
                    break;
                case PUBLISH:
                    msg = new PublishMessage(header, isServer);
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
                    return null;
            }

            msg.read(in);
        } finally {
            in.close();
        }

        return msg;
    }
}
