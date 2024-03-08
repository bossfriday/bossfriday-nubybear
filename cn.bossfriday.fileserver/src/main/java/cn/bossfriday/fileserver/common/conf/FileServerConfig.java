package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * FileServerConfig
 *
 * @author chenx
 */
@NoArgsConstructor
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileServerConfig {

    @Getter
    @XmlElement(name = "httpPort")
    private Integer httpPort;

    @Getter
    @XmlElement(name = "storageRootPath")
    private String storageRootPath;

    @Getter
    @XmlElement(name = "cleanerScanInterval")
    private Integer cleanerScanInterval;

    @Getter
    @XmlElementWrapper(name = "namespaces")
    @XmlElement(name = "namespace")
    private List<StorageNamespace> namespaces;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
