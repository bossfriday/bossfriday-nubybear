package cn.bossfriday.im.common.rpc.message;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private int code;

    private Object payload;

    private long appId;

    private String method;

    private String targetResourceId;

    private String logId;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
