package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.GsonUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class StorageNamespace {
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "expireDay")
    private int expireDay;

    public StorageNamespace() {

    }

    public String getName() {
        return name;
    }

    public int getExpireDay() {
        return expireDay;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}
