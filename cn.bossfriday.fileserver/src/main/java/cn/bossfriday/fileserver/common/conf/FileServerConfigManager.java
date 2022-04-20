package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.conf.ServiceConfig;
import cn.bossfriday.common.utils.XmlParserUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileServerConfigManager {
    private static FileServerConfig fileServerConfig;
    private static ServiceConfig serviceConfig;

    static {
        try {
            fileServerConfig = XmlParserUtil.parse("file-config.xml", FileServerConfig.class);
            serviceConfig = XmlParserUtil.parse("service-config.xml", ServiceConfig.class);
            log.info("load fileServerConfig done: " + fileServerConfig.toString());
            log.info("load serviceConfig done: " + serviceConfig.toString());
        } catch (Exception ex) {
            log.error("load FileServerConfig error!", ex);
        }
    }

    /**
     * getConfig
     */
    public static FileServerConfig getFileServerConfig() {
        return fileServerConfig;
    }

    /**
     * getServiceConfig
     */
    public static ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    /**
     * getCurrentClusterNodeName
     */
    public static String getCurrentClusterNodeName() {
        return serviceConfig.getClusterNode().getName();
    }
}
