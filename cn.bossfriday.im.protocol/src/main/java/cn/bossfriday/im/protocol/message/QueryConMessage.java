package cn.bossfriday.im.protocol.message;

import cn.bossfriday.im.protocol.core.MqttException;
import cn.bossfriday.im.protocol.core.MqttMessageHeader;
import cn.bossfriday.im.protocol.core.RetryableMqttMessage;
import cn.bossfriday.im.protocol.enums.MqttMessageType;
import cn.bossfriday.im.protocol.enums.QoS;

/**
 * Query confirm Message：客户端拉完消息之后要给服务端一个confirm；
 *
 * @author chenx
 */
public class QueryConMessage extends RetryableMqttMessage {

    public QueryConMessage(int messageId) {
        super(MqttMessageType.QUERYCON);
        this.setMessageSequence(messageId);
    }

    public QueryConMessage(MqttMessageHeader header) {
        super(header);
    }

    @Override
    public void setDup(boolean dup) {
        throw new MqttException("QueryConMessage don't use the DUP flag.");
    }

    @Override
    public void setRetained(boolean retain) {
        throw new MqttException("QueryConMessage don't use the RETAIN flag.");
    }

    @Override
    public void setQos(QoS qos) {
        throw new MqttException("QueryConMessage don't use the QoS flags.");
    }
}
