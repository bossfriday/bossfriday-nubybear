package cn.bossfriday.im.common.rpc.message;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSMessage
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SSMessage {

    private long appId;

    private String method;

    private String targetResourceId;

    private Object payload;

    private String logId;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
