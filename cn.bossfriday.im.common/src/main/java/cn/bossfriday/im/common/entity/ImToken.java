package cn.bossfriday.im.common.entity;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.im.common.biz.AppRegistrationManager;

/**
 * ImToken
 *
 * @author chenx
 */
public class ImToken {

    private long appId;

    private long appSecretHash;

    private String userId;

    private String deviceId;

    private long time;

    public ImToken() {

    }

    public ImToken(long appId, long appSecretHash, String userId, String deviceId, long time) {
        this.appId = appId;
        this.appSecretHash = appSecretHash;
        this.userId = userId;
        this.deviceId = deviceId;
        this.time = time;
    }

    public ImToken(long appId, String appSecret, String userId, String deviceId, long time) {
        this.appId = appId;
        this.appSecretHash = AppRegistrationManager.getAppSecretHashCode(appSecret);
        this.userId = userId;
        this.deviceId = deviceId;
        this.time = time;
    }

    public long getAppId() {
        return this.appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getAppSecretHash() {
        return this.appSecretHash;
    }

    public void setAppSecretHash(long appSecretHash) {
        this.appSecretHash = appSecretHash;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
