package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.conf.ServiceConfig;
import cn.bossfriday.common.conf.ServiceConfigLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * FileServerConfigManager
 *
 * @author chenx
 */
@Slf4j
public class FileServerConfigManager {

    private static FileServerConfig fileServerConfig;

    static {
        try {
            ServiceConfig<FileServerConfig> config = ServiceConfigLoader.getInstance(FileServerConfig.class).getServiceConfig();
            fileServerConfig = config.getConfig();
        } catch (Exception ex) {
            log.error("load FileServerConfig error!", ex);
        }
    }

    private FileServerConfigManager() {
        // do nothing
    }

    /**
     * getFileServerConfig
     */
    public static FileServerConfig getFileServerConfig() {
        return fileServerConfig;
    }
}
