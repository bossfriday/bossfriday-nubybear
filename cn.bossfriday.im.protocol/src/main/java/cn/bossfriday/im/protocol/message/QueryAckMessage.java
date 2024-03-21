package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;

import java.io.*;

import static cn.bossfriday.im.protocol.core.MqttException.READ_DATA_UNEXPECTED_EXCEPTION;

/**
 * QueryAckMessage
 *
 * @author chenx
 */
public class QueryAckMessage extends RetryableMqttMessage {

    private byte[] data;
    private int status;
    private int date;

    public QueryAckMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        super.readMessage(in, msgLength);
        DataInputStream dis = new DataInputStream(in);
        this.date = dis.readInt();
        this.status = in.read() * 0x100 + in.read();

        if (msgLength > 8) {
            int dataSize = msgLength - 8;
            this.data = new byte[dataSize];
            if (dis.read(this.data) != dataSize) {
                throw READ_DATA_UNEXPECTED_EXCEPTION;
            }
        }

        in.reset();

        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while ((line = bf.readLine()) != null) {
            buffer.append(line);
        }
    }

    public int getStatus() {
        return this.status;
    }

    public int getDate() {
        return this.date;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("QueryAckMessage don't use the DUP flag.");
    }
}
