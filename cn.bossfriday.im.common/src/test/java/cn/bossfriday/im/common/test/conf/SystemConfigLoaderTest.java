package cn.bossfriday.im.common.test.conf;

import cn.bossfriday.common.common.SystemConfig;
import cn.bossfriday.im.common.conf.SystemConfigLoader;
import cn.bossfriday.im.common.entity.conf.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;

/**
 * SystemConfigLoaderTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemConfigLoaderTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void getGlobalConfigAllTest() {
        SystemConfig systemConfig = SystemConfigLoader.getInstance().getSystemConfig();
        System.out.println(systemConfig);

        GlobalConfig globalConfig = SystemConfigLoader.getInstance().getGlobalConfig();
        System.out.println(globalConfig);

        FileServerConfig fileServerConfig = SystemConfigLoader.getInstance().getFileServerConfig();
        System.out.println(fileServerConfig);

        ImAccessConfig accessConfig = SystemConfigLoader.getInstance().getImAccessConfig();
        System.out.println(accessConfig);

        ImApiConfig imApiConfig = SystemConfigLoader.getInstance().getImApiConfig();
        System.out.println(imApiConfig);

        List<AppInfo> appRegistrationConfig = SystemConfigLoader.getInstance().getAppRegistrationConfig();
        System.out.println(appRegistrationConfig);

        HashMap<Long, AppInfo> appMap = SystemConfigLoader.getInstance().getAppMap();
        System.out.println(appMap.size());

        Assert.assertNotNull(systemConfig);
    }
}
