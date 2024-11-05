package cn.bossfriday.im.common.enums.file;

import lombok.Getter;

/**
 * FileStatus
 *
 * @author chenx
 */
public enum FileStatus {

    /**
     * 默认值
     */
    DEFAULT(0),

    /**
     * bit1：删除标志位
     */
    IS_BIT1(1),

    /**
     * bit2：预留标志位2
     */
    IS_BIT2(2),

    /**
     * bit3：预留标志位3
     */
    RESERVED_1(4),

    /**
     * bit4：预留标志位4
     */
    RESERVED_2(8),

    /**
     * bit5：预留标志位5
     */
    RESERVED_3(16),

    /**
     * bit6：：预留标志位6
     */
    RESERVED_4(32),

    /**
     * bit7：预留标志位7
     */
    RESERVED_5(64),

    /**
     * bit8：预留标志位8
     */
    RESERVED_6(128),
    ;

    @Getter
    private final int value;

    FileStatus(int value) {
        this.value = value;
    }
}
