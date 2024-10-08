package cn.bossfriday.im.access.common;

import io.netty.util.AttributeKey;

/**
 * AccessContextAttributeKey
 *
 * @author chenx
 */
public class AccessContextAttributeKey {

    public static final AttributeKey<Boolean> IS_CHANNEL_ACTIVE = AttributeKey.valueOf("AccessContextAttribute.isChannelActive");
    public static final AttributeKey<Boolean> IS_CONNECTED = AttributeKey.valueOf("AccessContextAttribute.isConnected");
    public static final AttributeKey<String> DISCONNECT_REASON = AttributeKey.valueOf("AccessContextAttribute.DisconnectReason");
}
