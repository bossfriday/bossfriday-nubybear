package cn.bossfriday.common.conf;

import cn.bossfriday.common.plugin.PluginElement;
import cn.bossfriday.common.router.ClusterNode;
import cn.bossfriday.common.utils.GsonUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * ServiceConfig
 *
 * @author chenx
 */
@XmlRootElement(name = "config")
public class ServiceConfig {

    /**
     * 集群名称（取一个合适的名称即可，ZK根节点以此命名）
     */
    @XmlElement(name = "systemName")
    private String systemName;

    /**
     * ZK地址（多个地址逗号分割）
     */
    @XmlElement(name = "zkAddress")
    private String zkAddress;

    /**
     * 集群节点
     */
    @XmlElement(name = "clusterNode", type = ClusterNode.class)
    private ClusterNode clusterNode;

    /**
     * 集群插件服务
     */
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
        return this.systemName;
    }

    public String getZkAddress() {
        return this.zkAddress;
    }

    public ClusterNode getClusterNode() {
        return this.clusterNode;
    }

    public List<PluginElement> getPluginElements() {
        return this.plugins;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}

