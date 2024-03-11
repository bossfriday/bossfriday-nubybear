package cn.bossfriday.im.common.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenMessageId
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenMessageId {

    /**
     * 系统内部消息ID
     */
    private String msgId;

    /**
     * 消息类型（对应MessageType枚举）
     */
    private byte msgType;

    /**
     * 消息时间
     */
    private long time;

    /**
     * 消息方向
     */
    private int msgDirection;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

}
