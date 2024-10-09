package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * ConnectMessage
 *
 * @author chenx
 */
public class ConnectMessage extends MqttMessage {

    private static final int CONNECT_HEADER_SIZE = 12;
    private static final byte[] DEFAULT_PROTOCOL_ID = {66, 111, 115, 115, 70, 114, 105, 100, 97, 121};
    private static final byte DEFAULT_PROTOCOL_VERSION = (byte) 1;

    private String protocolId = new String(DEFAULT_PROTOCOL_ID, StandardCharsets.UTF_8);
    private byte protocolVersion = DEFAULT_PROTOCOL_VERSION;

    private String clientId;
    private String clientIp;
    private int keepAlive;
    private String appId;
    private String token;
    private boolean cleanSession;
    private String willTopic;
    private String will;
    private QoS willQoS;
    private boolean retainWill;
    private boolean hasAppId;
    private boolean hasToken;
    private boolean hasWill;

    public ConnectMessage() {
        super(MqttMessageType.CONNECT);
    }

    public ConnectMessage(MqttMessageHeader header) {
        super(header);
    }

    public ConnectMessage(String clientId, String clientIp, boolean cleanSession, int keepAlive) {
        super(MqttMessageType.CONNECT);

        if (clientId == null || clientId.length() > 64) {
            throw new MqttException("Client id cannot be null and must be at most 64 characters long: " + clientId);
        }

        this.clientId = clientId;
        this.clientIp = clientIp;
        this.cleanSession = cleanSession;
        this.keepAlive = keepAlive;
    }

    @Override
    protected int getMessageLength() {
        int payloadSize = this.toUtfBytes(this.clientId).length;
        payloadSize += this.toUtfBytes(this.clientIp).length;
        payloadSize += this.toUtfBytes(this.willTopic).length;
        payloadSize += this.toUtfBytes(this.will).length;
        payloadSize += this.toUtfBytes(this.appId).length;
        payloadSize += this.toUtfBytes(this.token).length;

        return payloadSize + CONNECT_HEADER_SIZE;
    }

    @Override
    protected void readMessage(InputStream in, int msgLength) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        this.protocolId = dis.readUTF();
        this.protocolVersion = dis.readByte();
        byte cFlags = dis.readByte();
        this.hasAppId = (cFlags & 0x80) > 0;
        this.hasToken = (cFlags & 0x40) > 0;
        this.retainWill = (cFlags & 0x20) > 0;
        this.willQoS = QoS.valueOf(cFlags >> 3 & 0x03);
        this.hasWill = (cFlags & 0x04) > 0;
        this.cleanSession = (cFlags & 0x20) > 0;
        this.keepAlive = dis.read() * 256 + dis.read();
        this.clientId = dis.readUTF();
        this.clientIp = dis.readUTF();

        if (this.hasWill) {
            this.willTopic = dis.readUTF();
            this.will = dis.readUTF();
        }

        if (this.hasAppId) {
            try {
                this.appId = dis.readUTF();
            } catch (EOFException e) {
                // ignore
            }
        }

        if (this.hasToken) {
            try {
                this.token = dis.readUTF();
            } catch (EOFException e) {
                // ignore
            }
        }
    }

    @Override
    protected void writeMessage(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeUTF(this.protocolId);
        dos.write(this.protocolVersion);
        int flags = this.cleanSession ? 2 : 0;
        flags |= this.hasWill ? 0x04 : 0;
        flags |= this.willQoS == null ? 0 : this.willQoS.getValue() << 3;
        flags |= this.retainWill ? 0x20 : 0;
        flags |= this.hasToken ? 0x40 : 0;
        flags |= this.hasAppId ? 0x80 : 0;
        dos.write((byte) flags);
        dos.writeChar(this.keepAlive);
        dos.writeUTF(this.clientId);
        dos.writeUTF(this.clientIp);

        if (this.hasWill) {
            dos.writeUTF(this.willTopic);
            dos.writeUTF(this.will);
        }

        if (this.hasAppId) {
            dos.writeUTF(this.appId);
        }

        if (this.hasToken) {
            dos.writeUTF(this.token);
        }

        dos.flush();
    }

    public void setCredentials(String appId) {
        this.setCredentials(appId, null);
    }

    public void setCredentials(String appId, String token) {

        if ((appId == null || appId.isEmpty()) && (token != null && !token.isEmpty())) {
            throw new MqttException("It is not valid to supply a token without supplying a appId.");
        }

        this.appId = appId;
        this.token = token;
        this.hasAppId = this.appId != null;
        this.hasToken = this.token != null;
    }

    public void setWill(String willTopic, String will) {
        this.setWill(willTopic, will, QoS.AT_MOST_ONCE, false);
    }

    public void setWill(String willTopic, String will, QoS willQoS,
                        boolean retainWill) {
        if ((willTopic == null ^ will == null) || (will == null ^ willQoS == null)) {
            throw new MqttException("Can't set willTopic, will or willQoS value independently");
        }

        this.willTopic = willTopic;
        this.will = will;
        this.willQoS = willQoS;
        this.retainWill = retainWill;
        this.hasWill = willTopic != null;
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("ConnectMessage don't use the DUP flag.");
    }

    @Override
    public void setRetained(boolean retain) {
        throw new MqttException("ConnectMessage don't use the RETAIN flag.");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("ConnectMessage don't use the QoS flags.");
    }

    public String getProtocolId() {
        return this.protocolId;
    }

    public byte getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getClientId() {
        return this.clientId;
    }

    public int getKeepAlive() {
        return this.keepAlive;
    }

    public String getAppId() {
        return this.appId;
    }

    public String getToken() {
        return this.token;
    }

    public boolean isCleanSession() {
        return this.cleanSession;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public String getWillTopic() {
        return this.willTopic;
    }

    public String getWill() {
        return this.will;
    }

    public QoS getWillQoS() {
        return this.willQoS;
    }

    public boolean isWillRetained() {
        return this.retainWill;
    }

    public boolean hasAppId() {
        return this.hasAppId;
    }

    public boolean hasToken() {
        return this.hasToken;
    }

    public boolean hasWill() {
        return this.hasWill;
    }
}
