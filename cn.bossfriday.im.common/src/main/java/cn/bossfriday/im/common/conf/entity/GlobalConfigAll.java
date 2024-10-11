package cn.bossfriday.im.common.conf.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GlobalConfigAll
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfigAll {

    private GlobalConfig global;

    private List<AppInfo> appRegistration;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
