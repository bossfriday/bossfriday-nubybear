package cn.bossfriday.im.common.helper;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.id.SystemIdWorker;
import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
import cn.bossfriday.im.common.entity.conf.AppInfo;
import cn.bossfriday.im.common.enums.AppStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

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
     * getAppId
     *
     * @param appKey
     * @return
     */
    public static long getAppId(String appKey) {
        try {
            return SystemIdWorker.getId(appKey);
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * getAppInfo
     *
     * @param appId
     * @return
     */
    public static AppInfo getAppInfo(long appId) {
        AppInfo appInfo = ConfigurationAllLoader.getInstance().getAppMap().get(appId);
        if (Objects.isNull(appInfo)) {
            return null;
        }

        AppStatus appStatus = AppStatus.getByCode(appInfo.getStatus());
        if (Objects.isNull(appStatus)) {
            return null;
        }

        if (!appStatus.isOk()) {
            return null;
        }

        return appInfo;
    }

    /**
     * getAppInfo
     *
     * @param appKey
     * @return
     */
    public static AppInfo getAppInfo(String appKey) {
        long appId = getAppId(appKey);
        if (appId <= 0) {
            return null;
        }

        return getAppInfo(appId);
    }

    /**
     * isAppOk
     *
     * @param appId
     * @return
     */
    public static boolean isAppOk(long appId) {
        Map<Long, AppInfo> appMap = ConfigurationAllLoader.getInstance().getAppMap();
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
