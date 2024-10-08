package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.util.Objects;

/**
 * FileServerConfigManager
 *
 * @author chenx
 */
public class FileServerConfigLoader {

    private static SystemConfig<FileServerConfig> systemConfig;

    static {
        try {
            systemConfig = SystemConfigLoader.getInstance(FileServerConfig.class).getConfig();
        } catch (Exception ex) {
            throw new ServiceRuntimeException("FileServerConfigManager load config error! message: " + ex.getMessage());
        }
    }

    private FileServerConfigLoader() {
        // do nothing
    }

    /**
     * getSystemConfig
     *
     * @return
     */
    public static SystemConfig<FileServerConfig> getSystemConfig() {
        return systemConfig;
    }

    /**
     * getConfig
     */
    public static FileServerConfig getConfig() {
        if (Objects.isNull(systemConfig.getService())) {
            throw new ServiceRuntimeException("systemConfig.service is null!");
        }

        return systemConfig.getService();
    }

    /**
     * getClusterNodeName
     */
    public static String getClusterNodeName() {
        if (Objects.isNull(systemConfig)) {
            throw new ServiceRuntimeException("systemConfig is null!");
        }

        if (Objects.isNull(systemConfig.getClusterNode())) {
            throw new ServiceRuntimeException("systemConfig.clusterNode is null!");
        }

        return systemConfig.getClusterNode().getName();
    }
}
