package cn.bossfriday.im.access.common.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * ImAccessConfig
 *
 * @author chenx
 */
public class ImAccessConfig {

    @Getter
    @Setter
    private int tcpPort;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
