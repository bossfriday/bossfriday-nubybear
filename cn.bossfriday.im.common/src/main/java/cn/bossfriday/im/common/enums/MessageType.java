package cn.bossfriday.im.common.enums;

import cn.bossfriday.im.common.entity.appmsg.TextAppMessage;
import cn.bossfriday.im.common.entity.appmsg.core.AppMessage;
import org.apache.commons.lang.StringUtils;

/**
 * MessageType
 * <p>
 * 内置消息：常用消息格式，例如：文本消息，图片消息，位置消息，语音消息；
 * 非内置消息：可以理解位自定义消息，例如：“XXX正在输入”这种用自定义消息方式实现的功能；
 *
 * @author chenx
 */
public enum MessageType {

    /**
     * 内置消息（1 - 50）：预留50种应该足够表达目前已有内置消息；
     */
    NB_TXT_MSG((byte) 1, "NB:TxtMsg", TextAppMessage.class),
    NB_IMG_MSG((byte) 2, "NB:ImgMsg", null),
    NB_IMG_TXT_MSG((byte) 3, "NB:ImgTxtMsg", null),


    /**
     * 非内置消息（51 - 128）:将来如果不够则使用无符号1字节Int表达（最大255）
     */

    ;

    /**
     * 消息类型（1字节无符号Int）
     */
    private byte type;

    /**
     * 消息类型代码
     */
    private String code;

    /**
     * 消息实体对象类型
     */
    private Class<? extends AppMessage> appMessageType;

    MessageType(byte type, String code, Class<? extends AppMessage> appMessageType) {
        this.type = type;
        this.code = code;
        this.appMessageType = appMessageType;
    }

    public byte getType() {
        return this.type;
    }

    public String getCode() {
        return this.code;
    }

    public Class<? extends AppMessage> getAppMessageType() {
        return this.appMessageType;
    }

    /**
     * getByType
     *
     * @param type
     * @return
     */
    public static MessageType getByType(int type) {
        for (MessageType msgType : MessageType.values()) {
            if (msgType.getType() == (byte) type) {
                return msgType;
            }
        }

        return null;
    }

    /**
     * getMessageType
     *
     * @param type
     * @return
     */
    public static MessageType getByType(byte type) {
        return getByType(Byte.toUnsignedInt(type));
    }

    /**
     * getByCode
     *
     * @param code
     * @return
     */
    public static MessageType getByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }

        for (MessageType msgType : MessageType.values()) {
            if (msgType.getCode().equals(code)) {
                return msgType;
            }
        }

        return null;
    }
}
