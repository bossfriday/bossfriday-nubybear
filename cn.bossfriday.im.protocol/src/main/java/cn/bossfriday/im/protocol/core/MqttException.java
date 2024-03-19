package cn.bossfriday.im.protocol.core;

/**
 * MqttException
 *
 * @author chenx
 */
public class MqttException extends RuntimeException {

    public static final MqttException BAD_MESSAGE_EXCEPTION = new MqttException("Bad Message!");

    public static final MqttException READ_DATA_TIMEOUT_EXCEPTION = new MqttException("Read Data Timeout !");

    public static final MqttException OBFUSCATE_KEY_NOT_EXISTED_EXCEPTION = new MqttException("ObfuscateKey Not Existed!!");

    public MqttException(RuntimeException e) {
        super(e);
    }

    public MqttException(String msg) {
        super(msg);
    }

    public MqttException(String msg, RuntimeException e) {
        super(msg, e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
