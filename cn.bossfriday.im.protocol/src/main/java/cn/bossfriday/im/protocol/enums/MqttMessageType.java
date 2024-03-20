package cn.bossfriday.im.protocol.enums;

/**
 * MqttMessageType
 *
 * @author chenx
 */
public enum MqttMessageType {

    /**
     * connect
     */
    CONNECT(1),
    CONNACK(2),
    DISCONNECT(14),

    /**
     * publish
     */
    PUBLISH(3),
    PUBACK(4),

    /**
     * query
     */
    QUERY(5),
    QUERYACK(6),
    QUERYCON(7),

    /**
     * reconnect
     */
    RECONNECT(8),
    RECONNECTACK(9),

    /**
     * ping
     */
    PINGREQ(12),
    PINGRESP(13),

    /**
     * reserve
     */
    RESERVE1(0),
    RESERVE2(15);

    private final int value;

    MqttMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    /**
     * valueOf
     *
     * @param i
     * @return
     */
    public static MqttMessageType valueOf(int i) {
        for (MqttMessageType t : MqttMessageType.values()) {
            if (t.value == i) {
                return t;
            }
        }

        throw new IllegalArgumentException("Invalid MqttMessageType value: " + i);
    }
}