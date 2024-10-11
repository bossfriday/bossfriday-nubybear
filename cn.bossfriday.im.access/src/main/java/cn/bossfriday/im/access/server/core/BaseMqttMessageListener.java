package cn.bossfriday.im.access.server.core;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * BaseMqttMessageListener
 *
 * @author chenx
 */
public abstract class BaseMqttMessageListener<T extends MqttMessage> {

    protected T msg;
    protected ChannelHandlerContext ctx;

    protected BaseMqttMessageListener(T msg, ChannelHandlerContext ctx) {
        if (Objects.isNull(msg)) {
            throw new ServiceRuntimeException("MqttMessage is null!");
        }

        if (Objects.isNull(ctx)) {
            throw new ServiceRuntimeException("ChannelHandlerContext is null!");
        }

        this.msg = msg;
        this.ctx = ctx;
    }

    /**
     * onMqttMessageReceived
     */
    public abstract void onMqttMessageReceived();
}
