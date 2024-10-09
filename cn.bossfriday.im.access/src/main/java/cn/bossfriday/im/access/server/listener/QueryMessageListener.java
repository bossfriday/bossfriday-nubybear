package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.QueryMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * QueryMessageListener
 *
 * @author chenx
 */
public class QueryMessageListener extends BaseMqttMessageListener<QueryMessage> {

    public QueryMessageListener(QueryMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(QueryMessage msg, ChannelHandlerContext ctx) {

    }
}
