package cn.bossfriday.im.common.conf.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AppInfo
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfo {

    private long appId;

    private String appSecret;

    private String secureKey;

    private int status;

    private String fileServerAddress;

    private String apiServerAddress;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
