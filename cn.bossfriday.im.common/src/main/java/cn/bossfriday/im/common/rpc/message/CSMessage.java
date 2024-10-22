package cn.bossfriday.im.common.rpc.message;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSMessage
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CSMessage {

    private int messageSequence;

    private int qosFlags;

    private String clientOs;

    private String deviceId;

    private String sdkVersion;

    private byte protocolVersion;

    private long appId;

    private String method;

    private String requesterId;

    private String targetResourceId;

    private String channelId;

    private String appPackageName;

    private long sendTime;

    private String msgId;

    private int connectedTerminalCount;

    private Object payload;

    private String logId;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
