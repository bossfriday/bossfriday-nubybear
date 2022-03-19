package cn.bossfriday.common.conf;

import cn.bossfriday.common.plugin.PluginElement;
import cn.bossfriday.common.router.ClusterNode;
import cn.bossfriday.common.utils.GsonUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "config")
public class ServiceConfig {
    @XmlElement(name = "systemName")
    private String systemName;

    @XmlElement(name = "zkAddress")
    private String zkAddress;

    @XmlElement(name = "clusterNode", type = ClusterNode.class)
    private ClusterNode clusterNode;

    @XmlElementWrapper(name = "plugins")
    @XmlElement(name = "plugin")
    private List<PluginElement> plugins;

    public ServiceConfig() {

    }

    public ServiceConfig(String systemName, String zkAddress, ClusterNode clusterNode, List<PluginElement> plugins) {
        this.systemName = systemName;
        this.zkAddress = zkAddress;
        this.clusterNode = clusterNode;
        this.plugins = plugins;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }

    public List<PluginElement> getPluginElements() {
        return plugins;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

