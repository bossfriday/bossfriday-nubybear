package cn.bossfriday.im.common.entity.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * FileServerConfig
 *
 * @author chenx
 */
public class FileServerConfig {

    @Getter
    @Setter
    private Integer httpPort;

    @Getter
    @Setter
    private String storageRootPath;

    @Getter
    @Setter
    private Integer cleanerScanInterval;

    @Getter
    @Setter
    private List<StorageNamespace> namespaces;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
