package cn.bossfriday.im.protocol.core;

/**
 * MqttConstant
 *
 * @author chenx
 */
public class MqttConstant {

    private MqttConstant() {
        // just do nothing
    }

    public static final int MAX_MESSAGE_SEQUENCE = 65535;
    public static final int MAX_MESSAGE_LENGTH_SIZE = 3;

    public static final int STATUS_OK = 0;

    public static final int FIX_HEADER_LENGTH = 2;
}
