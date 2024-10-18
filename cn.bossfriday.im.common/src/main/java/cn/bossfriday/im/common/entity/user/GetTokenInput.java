package cn.bossfriday.im.common.entity.user;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GetTokenInput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTokenInput {

    private String apiVersion;

    private String userId;

    private String deviceId;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
