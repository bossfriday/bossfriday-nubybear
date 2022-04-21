package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.GsonUtil;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileServerConfig {
    @XmlElement(name = "httpPort")
    private Integer httpPort;

    @XmlElement(name = "storageRootPath")
    private String storageRootPath;

    @XmlElementWrapper(name = "namespaces")
    @XmlElement(name = "namespace")
    private List<StorageNamespace> namespaces;     // 存储空间规则

    public FileServerConfig() {

    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getStorageRootPath() {
        return storageRootPath;
    }

    public List<StorageNamespace> getNamespaces() {
        return namespaces;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}
