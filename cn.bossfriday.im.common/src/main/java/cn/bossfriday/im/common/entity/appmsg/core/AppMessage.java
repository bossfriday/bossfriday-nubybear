package cn.bossfriday.im.common.entity.appmsg.core;

import cn.bossfriday.common.utils.GsonUtil;
import com.google.gson.annotations.SerializedName;


/**
 * AppMessage
 *
 * @author chenx
 */
public class AppMessage {

    /**
     * 消息扩展信息
     */
    @SerializedName("extra")
    private String extra;

    /**
     * 消息内容中携带的发送者的用户信息
     */
    @SerializedName("user")
    private AppMessageSender user;

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public AppMessageSender getUser() {
        return this.user;
    }

    public void setUser(AppMessageSender user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
