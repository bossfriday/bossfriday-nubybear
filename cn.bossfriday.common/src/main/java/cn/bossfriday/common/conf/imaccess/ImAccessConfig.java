package cn.bossfriday.common.conf.imaccess;

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
