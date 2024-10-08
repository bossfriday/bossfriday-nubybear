package cn.bossfriday.im.access.common.conf;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.util.Objects;

/**
 * FileServerConfigManager
 *
 * @author chenx
 */
public class ImAccessConfigLoader {

    private static SystemConfig<ImAccessConfig> systemConfig;

    static {
        try {
            systemConfig = SystemConfigLoader.getInstance(ImAccessConfig.class).getConfig();
        } catch (Exception ex) {
            throw new ServiceRuntimeException("ImAccessConfigLoader load config error! message: " + ex.getMessage());
        }
    }

    private ImAccessConfigLoader() {
        // do nothing
    }

    /**
     * getSystemConfig
     *
     * @return
     */
    public static SystemConfig<ImAccessConfig> getSystemConfig() {
        return systemConfig;
    }

    /**
     * getConfig
     */
    public static ImAccessConfig getConfig() {
        if (Objects.isNull(systemConfig.getService())) {
            throw new ServiceRuntimeException("systemConfig.service is null!");
        }

        return systemConfig.getService();
    }
}
