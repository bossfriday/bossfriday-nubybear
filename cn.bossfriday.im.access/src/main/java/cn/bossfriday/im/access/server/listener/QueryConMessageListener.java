package cn.bossfriday.im.access.server.listener;

import cn.bossfriday.im.access.server.core.BaseMqttMessageListener;
import cn.bossfriday.im.protocol.message.QueryConMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * QueryConMessageListener
 *
 * @author chenx
 */
public class QueryConMessageListener extends BaseMqttMessageListener<QueryConMessage> {
    
    public QueryConMessageListener(QueryConMessage msg, ChannelHandlerContext ctx) {
        super(msg, ctx);
    }

    @Override
    protected void onMqttMessageReceived(QueryConMessage msg, ChannelHandlerContext ctx) {

    }
}
