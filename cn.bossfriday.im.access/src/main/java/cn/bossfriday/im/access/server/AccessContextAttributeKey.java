package cn.bossfriday.im.access.server;

import io.netty.util.AttributeKey;

/**
 * AccessContextAttributeKey
 *
 * @author chenx
 */
public class AccessContextAttributeKey {

    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("AccessContext.sessionId");

    public static final AttributeKey<Boolean> IS_CHANNEL_ACTIVE = AttributeKey.valueOf("AccessContext.isChannelActive");

    public static final AttributeKey<Boolean> IS_CONNECTED = AttributeKey.valueOf("AccessContext.isConnected");

    public static final AttributeKey<String> DISCONNECT_REASON = AttributeKey.valueOf("AccessContext.DisconnectReason");
}
