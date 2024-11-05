package cn.bossfriday.im.common.enums.file;

import static cn.bossfriday.im.common.constant.FileServerConstant.DEFAULT_STORAGE_ENGINE_VERSION;

/**
 * StorageEngineVersion
 *
 * @author chenx
 */
public enum StorageEngineVersion {

    /**
     * v1
     */
    V1(DEFAULT_STORAGE_ENGINE_VERSION);

    private final int value;

    StorageEngineVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
