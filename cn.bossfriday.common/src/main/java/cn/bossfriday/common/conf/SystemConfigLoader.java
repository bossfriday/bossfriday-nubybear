package cn.bossfriday.common.conf;

import cn.bossfriday.common.conf.fileserver.FileServerConfig;
import cn.bossfriday.common.conf.imaccess.ImAccessConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * SystemConfigLoader
 * <p>
 * 由于只是一个脚手架示例项目，为使得项目更加轻量，因此不想依赖Apollo或者其他配置中心，故全部走本地配置文件；
 *
 * @author chenx
 */
@Slf4j
public class SystemConfigLoader {

    private static final String CONFIG_FILE = "SystemConfig.yaml";
    private static final String CONFIG_NODE_NAME_SYSTEM_CONFIG = "system";
    private static final String CONFIG_NODE_NAME_FILE_SERVER_CONFIG = "fileServer";
    private static final String CONFIG_NODE_NAME_IM_ACCESS_CONFIG = "imAccess";

    @Getter
    private SystemConfig systemConfig;

    @Getter
    private FileServerConfig fileServerConfig;

    @Getter
    private ImAccessConfig imAccessConfig;

    private SystemConfigLoader() {
        this.loadConfig();
    }

    /**
     * SingletonHelper
     */
    private static class SingletonHelper {
        private static final SystemConfigLoader INSTANCE = new SystemConfigLoader();
    }

    /**
     * getInstance
     *
     * @return
     */
    public static SystemConfigLoader getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * loadConfig
     */
    private void loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = SystemConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Map<String, Object> configMap = yaml.load(inputStream);
            Object sysConfigObj = configMap.get(CONFIG_NODE_NAME_SYSTEM_CONFIG);
            Object fileServerConfigObj = configMap.get(CONFIG_NODE_NAME_FILE_SERVER_CONFIG);
            Object imAccessConfigObj = configMap.get(CONFIG_NODE_NAME_IM_ACCESS_CONFIG);

            if (Objects.nonNull(sysConfigObj)) {
                this.systemConfig = JSONUtil.toBean(JSONUtil.toJsonStr(sysConfigObj), SystemConfig.class);
            }

            if (Objects.nonNull(fileServerConfigObj)) {
                this.fileServerConfig = JSONUtil.toBean(JSONUtil.toJsonStr(fileServerConfigObj), FileServerConfig.class);
            }

            if (Objects.nonNull(imAccessConfigObj)) {
                this.imAccessConfig = JSONUtil.toBean(JSONUtil.toJsonStr(imAccessConfigObj), ImAccessConfig.class);
            }
        } catch (Exception ex) {
            throw new ServiceRuntimeException("load systemConfig error! message: " + ex.getMessage());
        }
    }
}
