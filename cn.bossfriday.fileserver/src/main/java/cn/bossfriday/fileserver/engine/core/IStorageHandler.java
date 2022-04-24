package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.StorageIndex;

public interface IStorageHandler {
    /**
     * getStorageIndex
     */
    StorageIndex getStorageIndex(String namespace) throws Exception;

    /**
     * askStorage(申请存储空间)
     */
    StorageIndex askStorage(StorageIndex storageIndex, long metaDataLength) throws Exception;
}
