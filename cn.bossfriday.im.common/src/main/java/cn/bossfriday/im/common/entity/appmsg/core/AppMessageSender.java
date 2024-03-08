package cn.bossfriday.im.common.entity.appmsg.core;

import cn.bossfriday.common.utils.GsonUtil;
import com.google.gson.annotations.SerializedName;

/**
 * AppMessageSender
 *
 * @author chenx
 */
public class AppMessageSender {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("alias")
    private String alias;

    @SerializedName("portrait")
    private String portrait;

    @SerializedName("extra")
    private String extra;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPortrait() {
        return this.portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
