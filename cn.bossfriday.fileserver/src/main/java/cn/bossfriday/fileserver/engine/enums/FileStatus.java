package cn.bossfriday.fileserver.engine.enums;

/**
 * FileStatus
 *
 * @author chenx
 */
public enum FileStatus {

    /**
     * Normal
     */
    NORMAL(0),

    /**
     * Deleted
     */
    DELETED(1);

    private final int value;

    FileStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
