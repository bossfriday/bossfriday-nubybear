package cn.bossfriday.fileserver.engine.enums;

import static cn.bossfriday.fileserver.common.FileServerConst.DEFAULT_STORAGE_ENGINE_VERSION;

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
