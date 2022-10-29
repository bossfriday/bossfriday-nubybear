package cn.bossfriday.fileserver.common.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * StorageNamespace
 *
 * @author chenx
 */
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class StorageNamespace {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "expireDay")
    private int expireDay;
}
