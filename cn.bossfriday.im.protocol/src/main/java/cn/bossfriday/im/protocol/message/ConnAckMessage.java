package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.enums.ConnectionStatus;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;

/**
 * ConnAckMessage
 *
 * @author chenx
 */
public class ConnAckMessage extends MqttMessage {

    private ConnectionStatus status;
    private String userId;

    public ConnAckMessage() {
        super(MqttMessageType.CONNACK);
    }

    public ConnAckMessage(MqttMessageHeader header) {
        super(header);
    }

    public ConnAckMessage(ConnectionStatus status) {
        super(MqttMessageType.CONNACK);
        if (status == null) {
            throw new MqttException("The status of ConnAskMessage can't be null");
        }

        this.status = status;
    }

    @Override
    protected int getMessageLength() {
        int length = FIX_HEADER_LENGTH;
        if (!StringUtils.isEmpty(this.userId)) {
            length += this.toUtfBytes(this.userId).length;
        }

        return length;
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        // Ignore first byte
        in.read();
        int result = in.read();
        switch (result) {
            case 0:
                this.status = ConnectionStatus.ACCEPTED;
                break;
            case 1:
                this.status = ConnectionStatus.UNACCEPTABLE_PROTOCOL_VERSION;
                break;
            case 2:
                this.status = ConnectionStatus.IDENTIFIER_REJECTED;
                break;
            case 3:
                this.status = ConnectionStatus.SERVER_UNAVAILABLE;
                break;
            case 4:
                this.status = ConnectionStatus.BAD_USERNAME_OR_PASSWORD;
                break;
            case 5:
                this.status = ConnectionStatus.NOT_AUTHORIZED;
                break;
            case 6:
                this.status = ConnectionStatus.REDIRECT;
                break;
            case 7:
                this.status = ConnectionStatus.PACKAGE_ERROR;
                break;
            case 8:
                this.status = ConnectionStatus.APP_BLOCK_OR_DELETE;
                break;
            case 9:
                this.status = ConnectionStatus.BLOCK;
                break;
            case 10:
                this.status = ConnectionStatus.TOKEN_EXPIRE;
                break;
            case 11:
                this.status = ConnectionStatus.DEVICE_ERROR;
                break;
            case 12:
                this.status = ConnectionStatus.HOSTNAME_ERROR;
                break;

            default:
                throw new MqttException("Unsupported CONNACK code: " + result);
        }

        if (msgLength > FIX_HEADER_LENGTH) {
            DataInputStream dis = new DataInputStream(in);
            this.userId = dis.readUTF();
        }
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        out.write(0x00);
        switch (this.status) {
            case ACCEPTED:
                out.write(0x00);
                break;
            case UNACCEPTABLE_PROTOCOL_VERSION:
                out.write(0x01);
                break;
            case IDENTIFIER_REJECTED:
                out.write(0x02);
                break;
            case SERVER_UNAVAILABLE:
                out.write(0x03);
                break;
            case BAD_USERNAME_OR_PASSWORD:
                out.write(0x04);
                break;
            case NOT_AUTHORIZED:
                out.write(0x05);
                break;
            case REDIRECT:
                out.write(0x06);
                break;
            case PACKAGE_ERROR:
                out.write(0x07);
                break;
            case APP_BLOCK_OR_DELETE:
                out.write(0x08);
                break;
            case BLOCK:
                out.write(0x09);
                break;

            default:
                throw new MqttException("Unsupported CONNACK code: " + this.status);
        }

        if (this.userId != null && !this.userId.isEmpty()) {
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(this.userId);
            dos.flush();
        }
    }

    public ConnectionStatus getStatus() {
        return this.status;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("ConnAckMessage don't use the DUP flag.");
    }

    @Override
    public void setRetained(boolean retain) {
        throw new MqttException("ConnAckMessage don't use the RETAIN flag.");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("ConnAckMessage don't use the QoS flags.");
    }
}
