package cn.bossfriday.im.common.conf;

import cn.bossfriday.im.common.entity.conf.AppInfo;
import cn.bossfriday.im.common.entity.conf.GlobalConfigAll;
import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalConfigAllLoader
 * <p>
 * 本项目只是一个 IM 脚手架项目，因此不想依赖DB和配置中心。
 * 正式项目的建议是：全局配置放到配置中心里，其他配置酌情配合缓存机制放到DB中，例如：appRegistration等；
 *
 * @author chenx
 */
@SuppressWarnings("squid:S6548")
public class GlobalConfigAllLoader {

    private static final String CONFIG_FILE = "GlobalConfigAll.yaml";

    private GlobalConfigAll globalConfigAll;
    private HashMap<Long, AppInfo> appMap;

    private GlobalConfigAllLoader() {
        this.load(CONFIG_FILE);
    }

    /**
     * getInstance
     */
    public static GlobalConfigAllLoader getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * getGlobalConfigAll
     */
    public GlobalConfigAll getGlobalConfigAll() {
        return this.globalConfigAll;
    }

    /**
     * getAppMap
     */
    public Map<Long, AppInfo> getAppMap() {
        return this.appMap;
    }

    /**
     * load
     */
    private void load(String fileName) {
        Yaml yaml = new Yaml(new Constructor(GlobalConfigAll.class));
        InputStream inputStream = GlobalConfigAllLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        this.globalConfigAll = yaml.load(inputStream);

        this.appMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(this.globalConfigAll.getAppRegistration())) {
            for (AppInfo entry : this.globalConfigAll.getAppRegistration()) {
                this.appMap.putIfAbsent(entry.getAppId(), entry);
            }
        }
    }

    /**
     * SingletonHelper
     */
    private static class SingletonHelper {
        private static final GlobalConfigAllLoader INSTANCE = new GlobalConfigAllLoader();
    }
}
