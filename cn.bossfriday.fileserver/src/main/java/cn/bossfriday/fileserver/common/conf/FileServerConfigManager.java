package cn.bossfriday.fileserver.common.conf;

import cn.bossfriday.common.utils.XmlParserUtil;
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
            fileServerConfig = XmlParserUtil.parse("file-config.xml", FileServerConfig.class);
            log.info("load fileServerConfig done: " + fileServerConfig.toString());
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
