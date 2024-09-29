package cn.bossfriday.common.conf;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.router.ClusterNode;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * ServiceConfigManager
 *
 * @author chenx
 */
@Slf4j
public class ServiceConfigLoader<T> {

    private static final String CONFIG_FILE_SERVICE = "serviceConfig.yaml";

    private static ServiceConfigLoader<?> instance;
    private final Class<T> configClass;
    private ServiceConfig<T> serviceConfig;

    private ServiceConfigLoader(Class<T> configClass) {
        this.configClass = configClass;
        this.loadConfig();
    }

    /**
     * getInstance
     *
     * @param configClass
     * @param <T>
     * @return
     */
    public static <T> ServiceConfigLoader<T> getInstance(Class<T> configClass) {
        if (instance == null) {
            synchronized (ServiceConfigLoader.class) {
                if (instance == null) {
                    instance = new ServiceConfigLoader<>(configClass);
                }
            }
        }

        return (ServiceConfigLoader<T>) instance;
    }

    /**
     * getServiceConfig
     *
     * @return
     */
    public ServiceConfig<T> getServiceConfig() {
        return this.serviceConfig;
    }

    /**
     * getClusterNodeName
     *
     * @return
     */
    public String getClusterNodeName() {
        if (Objects.isNull(this.serviceConfig)) {
            throw new ServiceRuntimeException("serviceConfig is null!");
        }

        if (Objects.isNull(this.serviceConfig.getClusterNode())) {
            throw new ServiceRuntimeException("serviceConfig.clusterNode is null!");
        }

        return this.serviceConfig.getClusterNode().getName();
    }

    /**
     * loadConfig
     */
    private void loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ServiceConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_SERVICE)) {
            Map<String, Object> configMap = yaml.load(inputStream);

            // 加载systemName、zkAddress
            this.serviceConfig = new ServiceConfig<>();
            this.serviceConfig.setSystemName((String) configMap.get("systemName"));
            this.serviceConfig.setZkAddress((String) configMap.get("zkAddress"));

            // 加载clusterNode
            Map<String, Object> clusterNodeMap = (Map<String, Object>) configMap.get("clusterNode");
            ClusterNode clusterNode = new ClusterNode();
            clusterNode.setName((String) clusterNodeMap.get("name"));
            clusterNode.setHost((String) clusterNodeMap.get("host"));
            clusterNode.setPort((Integer) clusterNodeMap.get("port"));
            clusterNode.setVirtualNodesNum((Integer) clusterNodeMap.get("virtualNodesNum"));
            this.serviceConfig.setClusterNode(clusterNode);

            // 加载config
            T config = JSONUtil.toBean(JSONUtil.toJsonStr(configMap.get("config")), this.configClass);
            this.serviceConfig.setConfig(config);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("load serviceConfig error! message: " + ex.getMessage());
        }
    }
}
