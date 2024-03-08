package cn.bossfriday.im.common.enums;

import cn.bossfriday.im.common.entity.appmsg.TextAppMessage;
import cn.bossfriday.im.common.entity.appmsg.core.AppMessage;
import org.apache.commons.lang.StringUtils;

/**
 * MessageType
 *
 * @author chenx
 */
public enum MessageType {

    /**
     * 内置消息（1 - 50）：预留50种应该足够表达目前已有内置消息；
     */
    NB_TXT_MSG((byte) 1, "RC:TxtMsg", TextAppMessage.class),


    /**
     * 非内置消息（51 - 128）:将来如果不够则使用无符号1字节Int表达（最大255）
     */

    ;

    private byte type;
    private String code;
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
