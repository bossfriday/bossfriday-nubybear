package cn.bossfriday.im.common.conf.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GlobalConfig
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfig {

    private long tokenExpireTime;
    
    private String fileServer;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
