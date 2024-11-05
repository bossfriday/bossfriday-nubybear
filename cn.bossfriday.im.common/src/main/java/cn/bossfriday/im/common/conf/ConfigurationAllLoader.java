package cn.bossfriday.im.common.conf;

import cn.bossfriday.common.common.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.common.entity.conf.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ConfigurationAllLoader
 * <p>
 * 由于只是一个脚手架示例项目，为使得项目更加轻量，因此不想依赖Apollo或者其他配置中心，故全部走本地配置文件；
 *
 * @author chenx
 */
@Slf4j
public class ConfigurationAllLoader {

    private static final String CONFIG_FILE = "ConfigurationAll.yaml";
    private static final String CONFIG_NODE_NAME_SYSTEM = "System";
    private static final String CONFIG_NODE_NAME_GLOBAL = "Global";
    private static final String CONFIG_NODE_NAME_FILE_SERVER = "FileServer";
    private static final String CONFIG_NODE_NAME_IM_ACCESS = "ImAccess";
    private static final String CONFIG_NODE_NAME_IM_API = "ImApi";
    private static final String CONFIG_NODE_NAME_APP_REGISTRATION = "DbApRegistration";

    @Getter
    private SystemConfig systemConfig;

    @Getter
    private GlobalConfig globalConfig;

    @Getter
    private FileServerConfig fileServerConfig;

    @Getter
    private ImAccessConfig imAccessConfig;

    @Getter
    private ImApiConfig imApiConfig;

    @Getter
    private List<AppInfo> appRegistrationConfig;

    @Getter
    private HashMap<Long, AppInfo> appMap;

    private ConfigurationAllLoader() {
        this.loadConfig();
    }

    /**
     * SingletonHelper
     */
    private static class SingletonHelper {
        private static final ConfigurationAllLoader INSTANCE = new ConfigurationAllLoader();
    }

    /**
     * getInstance
     *
     * @return
     */
    public static ConfigurationAllLoader getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * loadConfig
     */
    private void loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigurationAllLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Map<String, Object> configMap = yaml.load(inputStream);
            Object sysConfigObj = configMap.get(CONFIG_NODE_NAME_SYSTEM);
            Object globalConfigObj = configMap.get(CONFIG_NODE_NAME_GLOBAL);
            Object fileServerConfigObj = configMap.get(CONFIG_NODE_NAME_FILE_SERVER);
            Object imAccessConfigObj = configMap.get(CONFIG_NODE_NAME_IM_ACCESS);
            Object imApiConfigObj = configMap.get(CONFIG_NODE_NAME_IM_API);
            Object appRegistrationConfigObj = configMap.get(CONFIG_NODE_NAME_APP_REGISTRATION);

            if (Objects.nonNull(sysConfigObj)) {
                this.systemConfig = JSONUtil.toBean(JSONUtil.toJsonStr(sysConfigObj), SystemConfig.class);
            }

            if (Objects.nonNull(globalConfigObj)) {
                this.globalConfig = JSONUtil.toBean(JSONUtil.toJsonStr(globalConfigObj), GlobalConfig.class);
            }

            if (Objects.nonNull(fileServerConfigObj)) {
                this.fileServerConfig = JSONUtil.toBean(JSONUtil.toJsonStr(fileServerConfigObj), FileServerConfig.class);
            }

            if (Objects.nonNull(imAccessConfigObj)) {
                this.imAccessConfig = JSONUtil.toBean(JSONUtil.toJsonStr(imAccessConfigObj), ImAccessConfig.class);
            }

            if (Objects.nonNull(imApiConfigObj)) {
                this.imApiConfig = JSONUtil.toBean(JSONUtil.toJsonStr(imApiConfigObj), ImApiConfig.class);
            }

            if (Objects.nonNull(appRegistrationConfigObj)) {
                JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.toJsonStr(appRegistrationConfigObj));
                this.appRegistrationConfig = jsonArray.stream()
                        .map(json -> JSONUtil.toBean(json.toString(), AppInfo.class))
                        .collect(Collectors.toList());

                this.appMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(this.appRegistrationConfig)) {
                    this.appRegistrationConfig.forEach(entry -> this.appMap.putIfAbsent(entry.getAppId(), entry));
                }
            }
        } catch (Exception ex) {
            throw new ServiceRuntimeException("load systemConfig error! message: " + ex.getMessage());
        }
    }
}
