package cn.bossfriday.im.access.server;

import cn.bossfriday.common.exception.ServiceException;
import cn.bossfriday.im.protocol.message.*;
import io.netty.channel.ChannelHandlerContext;

/**
 * IMqttListener
 *
 * @author chenx
 */
public interface IMqttListener {

    /**
     * channelActive
     */
    void channelActive(ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onConnectMessage
     */
    void onConnectMessage(ConnectMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onDisconnectMessage
     */
    void onDisconnectMessage(DisconnectMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onPublishMessage
     */
    void onPublishMessage(PublishMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onPubAckMessage
     */
    void onPubAckMessage(PubAckMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onQueryMessage
     */
    void onQueryMessage(QueryMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onQueryConMessage
     */
    void onQueryConMessage(QueryConMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * onPingReqMessage
     */
    void onPingReqMessage(PingReqMessage msg, ChannelHandlerContext ctx) throws ServiceException;

    /**
     * closed
     */
    void closed(ChannelHandlerContext ctx) throws ServiceException;

    /**
     * exceptionCaught
     */
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws ServiceException;
}
