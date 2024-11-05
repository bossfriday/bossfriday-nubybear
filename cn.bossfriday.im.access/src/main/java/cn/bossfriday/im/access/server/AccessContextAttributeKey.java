package cn.bossfriday.im.access.server;

import cn.bossfriday.im.common.enums.access.ConnectState;
import cn.bossfriday.im.protocol.client.ClientInfo;
import io.netty.util.AttributeKey;

/**
 * AccessContextAttributeKey
 *
 * @author chenx
 */
public class AccessContextAttributeKey {

    private AccessContextAttributeKey() {
        // do nothing
    }

    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("AccessContext.sessionId");

    public static final AttributeKey<Boolean> IS_CHANNEL_ACTIVE = AttributeKey.valueOf("AccessContext.isChannelActive");

    public static final AttributeKey<Boolean> IS_CONNECTED = AttributeKey.valueOf("AccessContext.isConnected");

    public static final AttributeKey<String> DISCONNECT_REASON = AttributeKey.valueOf("AccessContext.disconnectReason");

    public static final AttributeKey<ConnectState> CONN_STATE = AttributeKey.valueOf("AccessContext.connectState");

    public static final AttributeKey<ClientInfo> CLIENT_INFO = AttributeKey.valueOf("AccessContext.clientInfo");
}
