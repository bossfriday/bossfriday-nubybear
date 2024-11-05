package cn.bossfriday.im.sdk.test.tools;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.im.api.helper.ApiHelper;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
import cn.bossfriday.im.common.entity.conf.AppInfo;
import cn.bossfriday.im.common.helper.AppHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

/**
 * ApiTools
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiTools {


    @Before
    public void mockInit() {

    }

    @Test
    public void getApiAuthHeads() {
        List<AppInfo> appInfoList = ConfigurationAllLoader.getInstance().getAppRegistrationConfig();
        if (CollectionUtils.isEmpty(appInfoList)) {
            throw new ServiceRuntimeException("appInfoList is empty!");
        }

        AppInfo appInfo = appInfoList.get(0);
        String appKey = AppHelper.getAppKey(appInfo.getAppId());
        Map<String, String> authHeadMap = ApiHelper.getSignatureHeaderMap(appKey, appInfo.getAppSecret());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : authHeadMap.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue());
            sb.append("\r\n");
        }

        System.out.println(sb.toString());
        Assert.assertTrue(true);
    }
}
