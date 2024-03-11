package cn.bossfriday.im.common.enums;

import static cn.bossfriday.common.utils.ByteUtil.MAX_VALUE_UNSIGNED_INT8;

/**
 * MessageDirection
 *
 * @author chenx
 */
public enum MessageDirection {

    /**
     * application to user
     */
    APPLICATION_TO_USER(1),

    /**
     * user to application
     */
    USER_TO_APPLICATION(2);

    private int code;

    MessageDirection(int code) {
        if (code < 0 || code > MAX_VALUE_UNSIGNED_INT8) {
            throw new IllegalArgumentException("code must range in [0, " + MAX_VALUE_UNSIGNED_INT8 + "]");
        }

        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    /**
     * getByCode
     *
     * @param code
     * @return
     */
    public static MessageDirection getByCode(int code) {
        for (MessageDirection entry : MessageDirection.values()) {
            if (entry.code == code) {
                return entry;
            }
        }

        return null;
    }
}
