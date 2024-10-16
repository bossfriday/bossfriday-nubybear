package cn.bossfriday.common.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * ImApiConfig
 *
 * @author chenx
 */
public class ImApiConfig {

    @Getter
    @Setter
    private int httpPort;

    @Getter
    @Setter
    private String fileServerAddress;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
