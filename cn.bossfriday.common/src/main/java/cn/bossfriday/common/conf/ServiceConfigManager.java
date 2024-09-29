package cn.bossfriday.common.conf;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.XmlParserUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * ServiceConfigManager
 *
 * @author chenx
 */
@Slf4j
public class ServiceConfigManager {

    private static final String SERVICE_CONFIG_PATH = "service-config.xml";
    private static ServiceConfig serviceConfig;

    static {
        try {
            serviceConfig = XmlParserUtil.parse(SERVICE_CONFIG_PATH, ServiceConfig.class);
            log.info("load serviceConfig done: " + serviceConfig.toString());
        } catch (Exception ex) {
            log.error("load serviceConfig error!", ex);
            throw new ServiceRuntimeException("load serviceConfig error!");
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
