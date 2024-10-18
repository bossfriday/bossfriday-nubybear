package cn.bossfriday.im.common.entity.user;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GetTokenOutput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTokenOutput {

    private String userId;

    private String token;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
