package cn.bossfriday.im.common.helper;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.im.common.conf.GlobalConfigAllLoader;
import cn.bossfriday.im.common.conf.entity.AppInfo;
import cn.bossfriday.im.common.enums.AppStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * AppHelper
 *
 * @author chenx
 */
public class AppHelper {

    private AppHelper() {
        // do nothing
    }

    /**
     * getAppInfo
     *
     * @param appId
     * @return
     */
    public static AppInfo getAppInfo(long appId) {
        return GlobalConfigAllLoader.getInstance().getAppMap().get(appId);
    }

    /**
     * isAppOk
     *
     * @param appId
     * @return
     */
    public static boolean isAppOk(long appId) {
        Map<Long, AppInfo> appMap = GlobalConfigAllLoader.getInstance().getAppMap();
        if (!appMap.containsKey(appId)) {
            return false;
        }

        return AppStatus.isAppOk(appMap.get(appId).getStatus());
    }

    /**
     * getAppSecretHashCode
     *
     * @param appSecret
     * @return
     */
    public static long getAppSecretHashCode(String appSecret) {
        if (StringUtils.isEmpty(appSecret)) {
            throw new ServiceRuntimeException("appSecret is empty!");
        }

        return MurmurHashUtil.hash64(appSecret);
    }

}
