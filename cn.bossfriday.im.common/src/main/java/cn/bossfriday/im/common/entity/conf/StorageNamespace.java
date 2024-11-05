package cn.bossfriday.im.common.entity.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * StorageNamespace
 *
 * @author chenx
 */
public class StorageNamespace {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private int expireDay;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
