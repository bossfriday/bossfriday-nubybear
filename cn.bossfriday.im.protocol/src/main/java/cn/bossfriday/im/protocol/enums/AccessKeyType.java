package cn.bossfriday.im.protocol.enums;

/**
 * CmpKeyType
 *
 * @author chenx
 */
public enum AccessKeyType {

    /**
     * 104
     */
    KEY_104(104),

    /**
     * 105
     */
    KEY_105(105),

    /**
     * 106
     */
    KEY_106(106),

    /**
     * 107
     */
    KEY_107(107),

    /**
     * 108（Beem）
     */
    KEY_108(108);

    private final int code;

    AccessKeyType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
