package cn.bossfriday.im.protocol.enums;

/**
 * QoS
 *
 * @author chenx
 */
public enum QoS {

    AT_MOST_ONCE(0),

    AT_LEAST_ONCE(1),

    EXACTLY_ONCE(2),

    DEFAULT(3);

    private int value;

    QoS(int value) {
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
    public static QoS valueOf(int i) {
        for (QoS q : QoS.values()) {
            if (q.value == i) {
                return q;
            }
        }

        throw new IllegalArgumentException("Invalid QoS value: " + i);
    }
}
