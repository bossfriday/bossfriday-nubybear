package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class FileServerConfig {
    @XmlElement(name = "httpPort")
    private Integer httpPort;

    @XmlElement(name = "storageRootPath")
    private String storageRootPath;

    public FileServerConfig() {

    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getStorageRootPath() {
        return storageRootPath;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}
