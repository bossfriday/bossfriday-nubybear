package cn.bossfriday.im.access.server.core;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * MqttMessageListener
 *
 * @author chenx
 */
public abstract class MqttMessageListener<T extends MqttMessage> {

    private T msg;
    private ChannelHandlerContext ctx;

    protected MqttMessageListener(T msg, ChannelHandlerContext ctx) {
        this.msg = msg;
        this.ctx = ctx;
    }

    /**
     * onMessageReceived
     */
    public void onMessageReceived() {
        if (Objects.isNull(this.msg)) {
            throw new ServiceRuntimeException("MqttMessage is null!");
        }

        if (Objects.isNull(this.ctx)) {
            throw new ServiceRuntimeException("ChannelHandlerContext is null!");
        }

        this.onMqttMessageReceived(this.msg, this.ctx);
    }

    /**
     * onMqttMessageReceived
     */
    protected abstract void onMqttMessageReceived(T msg, ChannelHandlerContext ctx);
}
