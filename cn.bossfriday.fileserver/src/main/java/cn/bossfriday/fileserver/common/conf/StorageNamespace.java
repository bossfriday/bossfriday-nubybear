package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class StorageNamespace {

    @Getter
    @XmlAttribute(name = "name")
    private String name;

    @Getter
    @XmlAttribute(name = "expireDay")
    private int expireDay;

    public StorageNamespace() {

    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}
