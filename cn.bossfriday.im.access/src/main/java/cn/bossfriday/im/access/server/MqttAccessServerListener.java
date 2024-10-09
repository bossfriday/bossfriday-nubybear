package cn.bossfriday.im.access.server;

import cn.bossfriday.common.exception.ServiceException;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.protocol.message.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static cn.bossfriday.im.access.server.AccessContextAttributeKey.SESSION_ID;

/**
 * MqttAccessServerListener
 *
 * @author chenx
 */
@Slf4j
public class MqttAccessServerListener implements IMqttListener {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws ServiceException {
        ctx.channel().attr(SESSION_ID).set(UUIDUtil.getShortString(UUID.randomUUID()));
    }

    @Override
    public void onConnectMessage(ConnectMessage msg, ChannelHandlerContext ctx) throws ServiceException {
        //String clientIp = msg.getClientIp();
    }

    @Override
    public void onDisconnectMessage(DisconnectMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void onPublishMessage(PublishMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void onPubAckMessage(PubAckMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void onQueryMessage(QueryMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void onQueryConMessage(QueryConMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void onPingReqMessage(PingReqMessage msg, ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void closed(ChannelHandlerContext ctx) throws ServiceException {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws ServiceException {

    }
}
