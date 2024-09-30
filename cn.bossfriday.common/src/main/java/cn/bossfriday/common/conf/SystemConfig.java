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
public class SystemConfig<T> {

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
     * 集群插件服务(有配置走配置，无配置反射获取当前jar包内所有UntypedActor类；)
     */
    @Getter
    @Setter
    private List<PluginElement> plugins;

    /**
     * 服务配置
     */
    @Getter
    @Setter
    private T service;

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}

