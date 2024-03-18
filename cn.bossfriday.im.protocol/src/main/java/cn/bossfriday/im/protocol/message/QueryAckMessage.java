package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;

import java.io.*;

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
            this.data = new byte[msgLength - 8];
            dis.read(this.data);
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
