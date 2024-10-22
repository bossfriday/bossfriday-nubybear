package cn.bossfriday.im.common.rpc;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ActorContext
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActorContext {

    private Class<?> requestClass;

    private boolean fromApi;

    private int resultCode;

    private long appId;

    private String method;

    private int requestMessageId;

    private String clientOs;

    private String deviceId;

    private String sdkVersion;

    private byte protocolVersion;

    private String requesterId;

    private int qosFlags;

    private String targetResourceId;

    private String channelId;

    private String appPackageName;

    private long sendTime = 0;

    private String msgId = "";

    private int connectedTerminalCount;

    private String userId;

    private String userName;

    private String clientAddr;

    private String logId;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
