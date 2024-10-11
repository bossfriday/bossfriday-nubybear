package cn.bossfriday.im.common.test.conf;

import cn.bossfriday.im.common.conf.GlobalConfigAllLoader;
import cn.bossfriday.im.common.conf.entity.GlobalConfigAll;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * GlobalConfigAllLoaderTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalConfigAllLoaderTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void getGlobalConfigAllTest() {
        GlobalConfigAll globalConfigAll = GlobalConfigAllLoader.getInstance().getGlobalConfigAll();
        System.out.println(globalConfigAll);
        Assert.assertNotNull(globalConfigAll);
    }
}
