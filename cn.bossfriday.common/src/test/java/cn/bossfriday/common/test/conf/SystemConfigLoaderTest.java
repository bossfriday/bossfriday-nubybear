package cn.bossfriday.common.test.conf;

import cn.bossfriday.common.conf.SystemConfigLoader;

/**
 * SystemConfigLoaderTest
 *
 * @author chenx
 */
public class SystemConfigLoaderTest {

    public static void main(String[] args) {
        System.out.println(SystemConfigLoader.getInstance().getSystemConfig());
        System.out.println(SystemConfigLoader.getInstance().getFileServerConfig());
        System.out.println(SystemConfigLoader.getInstance().getImAccessConfig());
    }
}
