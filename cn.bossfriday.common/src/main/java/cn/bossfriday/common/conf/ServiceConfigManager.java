package cn.bossfriday.common.conf;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Objects;

/**
 * ServiceConfigManager
 *
 * @author chenx
 */
@Slf4j
public class ServiceConfigManager {

    private static final String CONFIG_FILE_SERVICE = "serviceConfig.yaml";
    private static ServiceConfig serviceConfig;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ServiceConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE_SERVICE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("service config file not existed! file: " + CONFIG_FILE_SERVICE);
            }

            serviceConfig = yaml.loadAs(inputStream, ServiceConfig.class);
        } catch (Exception e) {
            throw new ServiceRuntimeException("load serviceConfig error! message: " + e.getMessage());
        }
    }

    private ServiceConfigManager() {
        // do nothing
    }

    /**
     * getServiceConfig
     */
    public static ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    /**
     * getClusterNodeName
     */
    public static String getClusterNodeName() {
        if (Objects.isNull(serviceConfig)) {
            throw new ServiceRuntimeException("serviceConfig is null!");
        }

        if (Objects.isNull(serviceConfig.getClusterNode())) {
            throw new ServiceRuntimeException("serviceConfig.clusterNode is null!");
        }

        return serviceConfig.getClusterNode().getName();
    }
}
