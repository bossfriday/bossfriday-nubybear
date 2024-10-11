package cn.bossfriday.im.common.conf;

import cn.bossfriday.im.common.conf.entity.GlobalConfigAll;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

/**
 * GlobalConfigAllLoader
 * <p>
 * 本项目只是一个脚手架项目，因此不依赖DB和配置中心。
 * 正式项目的建议是：全局配置放到配置中心里，其他配置酌情配合缓存机制放到DB中，例如：appRegistration等；
 *
 * @author chenx
 */
public class GlobalConfigAllLoader {

    private static final String CONFIG_FILE = "GlobalConfigAll.yaml";
    private static GlobalConfigAllLoader instance;
    private GlobalConfigAll globalConfigAll;

    private GlobalConfigAllLoader() {
        this.load(CONFIG_FILE);
    }

    /**
     * getInstance
     *
     * @return
     */
    public static GlobalConfigAllLoader getInstance() {
        if (instance == null) {
            synchronized (GlobalConfigAllLoader.class) {
                if (instance == null) {
                    instance = new GlobalConfigAllLoader();
                }
            }
        }

        return instance;
    }

    /**
     * getGlobalConfigAll
     *
     * @return
     */
    public GlobalConfigAll getGlobalConfigAll() {
        return this.globalConfigAll;
    }


    /**
     * load
     */
    public void load(String fileName) {
        Yaml yaml = new Yaml(new Constructor(GlobalConfigAll.class));
        InputStream inputStream = GlobalConfigAllLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        this.globalConfigAll = yaml.load(inputStream);
    }
}
