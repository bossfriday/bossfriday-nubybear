package cn.bossfriday.common.conf;

import cn.bossfriday.common.plugin.PluginElement;
import cn.bossfriday.common.router.ClusterNode;
import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ServiceConfig
 *
 * @author chenx
 */
public class SystemConfig {

    /**
     * 集群名称（取一个合适的名称即可，ZK根节点以此命名）
     */
    @Getter
    @Setter
    private String systemName;

    /**
     * ZK地址（多个地址逗号分割）
     */
    @Getter
    @Setter
    private String zkAddress;

    /**
     * 集群节点
     */
    @Getter
    @Setter
    private ClusterNode clusterNode;

    /**
     * 集群服务
     */
    @Getter
    @Setter
    private List<PluginElement> plugins;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}

