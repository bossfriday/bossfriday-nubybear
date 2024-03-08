package cn.bossfriday.im.common.entity.appmsg;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.im.common.entity.appmsg.core.AppMessage;
import com.google.gson.annotations.SerializedName;

/**
 * TextAppMessage
 *
 * @author chenx
 */
public class TextAppMessage extends AppMessage {

    @SerializedName("content")
    private String content;

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
