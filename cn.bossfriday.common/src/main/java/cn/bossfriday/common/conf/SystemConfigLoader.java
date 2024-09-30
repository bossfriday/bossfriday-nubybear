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
 * SystemConfigLoader
 *
 * @author chenx
 */
@Slf4j
public class SystemConfigLoader<T> {

    private static final String CONFIG_FILE_SERVICE = "systemConfig.yaml";

    private static SystemConfigLoader<?> instance;
    private final Class<T> serviceConfigClass;
    private SystemConfig<T> systemConfig;

    private SystemConfigLoader(Class<T> serviceConfigClass) {
        this.serviceConfigClass = serviceConfigClass;
        this.loadConfig();
    }

    /**
     * getInstance
     *
     * @param configClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("squid:S2168")
    public static <T> SystemConfigLoader<T> getInstance(Class<T> configClass) {
        if (instance == null) {
            synchronized (SystemConfigLoader.class) {
                if (instance == null) {
                    instance = new SystemConfigLoader<>(configClass);
                }
            }
        }

        return (SystemConfigLoader<T>) instance;
    }

    /**
     * getConfig
     *
     * @return
     */
    public SystemConfig<T> getConfig() {
        return this.systemConfig;
    }

    /**
     * loadConfig
     */
    private void loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = SystemConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_SERVICE)) {
            Map<String, Object> configMap = yaml.load(inputStream);
            this.systemConfig = new SystemConfig<>();

            // 加载系统配置
            Map<String, Object> systemMap = (Map<String, Object>) configMap.get("system");
            this.systemConfig.setSystemName((String) systemMap.get("systemName"));
            this.systemConfig.setZkAddress((String) systemMap.get("zkAddress"));

            Map<String, Object> clusterNodeMap = (Map<String, Object>) systemMap.get("clusterNode");
            ClusterNode clusterNode = new ClusterNode();
            clusterNode.setName((String) clusterNodeMap.get("name"));
            clusterNode.setHost((String) clusterNodeMap.get("host"));
            clusterNode.setPort((Integer) clusterNodeMap.get("port"));
            clusterNode.setVirtualNodesNum((Integer) clusterNodeMap.get("virtualNodesNum"));
            this.systemConfig.setClusterNode(clusterNode);

            // 加载服务配置
            if (Objects.nonNull(configMap.get("service"))) {
                T serviceConfig = JSONUtil.toBean(JSONUtil.toJsonStr(configMap.get("service")), this.serviceConfigClass);
                this.systemConfig.setService(serviceConfig);
            }
        } catch (Exception ex) {
            throw new ServiceRuntimeException("load systemConfig error! message: " + ex.getMessage());
        }
    }
}
