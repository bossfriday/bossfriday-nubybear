package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.enums.DisconnectionStatus;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;

/**
 * DisconnectMessage
 *
 * @author chenx
 */
public class DisconnectMessage extends MqttMessage {


    private DisconnectionStatus status;

    public DisconnectMessage(MqttMessageHeader header) throws IOException {
        super(header);
    }

    public DisconnectMessage(DisconnectionStatus status) {
        super(MqttMessageType.DISCONNECT);
        if (status == null) {
            throw new MqttException("The status of ConnAskMessage can't be null");
        }

        this.status = status;
    }

    public DisconnectMessage() {
        super(MqttMessageType.DISCONNECT);
    }

    @Override
    protected int getMessageLength() {
        return FIX_HEADER_LENGTH;
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        // 忽略DisconnectMessage状态字段
        in.read();
        int result = in.read();
        switch (result) {
            case 0:
                this.status = DisconnectionStatus.RECONNECT;
                break;
            case 1:
                this.status = DisconnectionStatus.OTHER_DEVICE_LOGIN;
                break;
            case 2:
                this.status = DisconnectionStatus.CLOSURE;
                break;
            case 3:
                this.status = DisconnectionStatus.NO;
                break;
            case 4:
                this.status = DisconnectionStatus.LOGOUT;
                break;

            default:
                throw new MqttException("Unsupported CONNACK code: " + result);
        }
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        // DisconnectMessage状态字段：0x00表示没有特定的断开连接状态
        out.write(0x00);
        switch (this.status) {
            case RECONNECT:
                out.write(0x00);
                break;
            case OTHER_DEVICE_LOGIN:
                out.write(0x01);
                break;
            case CLOSURE:
                out.write(0x02);
                break;
            case NO:
                out.write(0x03);
                break;
            case LOGOUT:
                out.write(0x04);
                break;
            default:
                throw new MqttException("Unsupported CONNACK code: " + this.status);
        }
    }

    public DisconnectionStatus getStatus() {
        return this.status;
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("DisconnectMessage does not support the DUP flag");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("DisconnectMessage does not support the QoS flag");
    }
}
