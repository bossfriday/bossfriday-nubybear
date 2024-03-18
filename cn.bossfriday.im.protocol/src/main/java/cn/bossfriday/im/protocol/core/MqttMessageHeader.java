package cn.bossfriday.im.protocol.core;

import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

/**
 * MqttMessageHeader
 * <p>
 * | MsgType (消息类型：4 bits) |  DUP (重传标记：1 bits)  |   QoS (质量等级：2 bits)  |  RETAIN (保留位：1 bits)  |
 *
 * @author chenx
 */
public class MqttMessageHeader {

    private MqttMessageType mqttMessageType;
    private boolean retain;
    private QoS qos = QoS.AT_MOST_ONCE;
    private boolean dup;

    public MqttMessageHeader(MqttMessageType mqttMessageType, boolean retain, QoS qos, boolean dup) {
        this.mqttMessageType = mqttMessageType;
        this.retain = retain;
        this.qos = qos;
        this.dup = dup;
    }

    public MqttMessageHeader(byte flags) {
        this.retain = (flags & 1) > 0;
        this.qos = QoS.valueOf((flags & 0x6) >> 1);
        this.dup = (flags & 8) > 0;
        this.mqttMessageType = MqttMessageType.valueOf((flags >> 4) & 0xF);
    }

    public MqttMessageType getType() {
        return this.mqttMessageType;
    }

    public MqttMessageType getMqttMessageType() {
        return this.mqttMessageType;
    }

    public boolean isRetained() {
        return this.retain;
    }

    public QoS getQos() {
        return this.qos;
    }

    public boolean isDup() {
        return this.dup;
    }

    public void setMqttMessageType(MqttMessageType mqttMessageType) {
        this.mqttMessageType = mqttMessageType;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }

    /**
     * encode
     * <p>
     * MsgType (消息类型：4 bits)
     * DUP (重传标记：1 bits)
     * QoS (质量等级：2 bits)
     * RETAIN (保留位：1 bits)
     *
     * @return
     */
    public byte encode() {
        byte b = 0;
        b = (byte) (this.mqttMessageType.getValue() << 4);
        b |= this.retain ? 1 : 0;
        b |= this.qos.getValue() << 1;
        b |= this.dup ? 8 : 0;

        return b;
    }

    @Override
    public String toString() {
        return "MqttMessageHeader{" + "mqttMessageType=" + this.mqttMessageType + ", retain=" + this.retain + ", qos=" + this.qos + ", dup=" + this.dup + '}';
    }
}
