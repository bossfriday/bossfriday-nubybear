package cn.bossfriday.im.protocol.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * MessageOutputStream
 *
 * @author chenx
 */
public class MessageOutputStream {

    private final OutputStream out;

    public MessageOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * writeMessage
     *
     * @param msg
     * @throws IOException
     */
    public void writeMessage(MqttMessage msg) throws IOException {
        msg.write(this.out);
        this.out.flush();
    }
}
