package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.engine.entity.RecoverableTmpFile;
import cn.bossfriday.fileserver.engine.entity.StorageIndex;

public interface IStorageHandler {
    /**
     * getStorageIndex
     */
    StorageIndex getStorageIndex(String namespace) throws Exception;

    /**
     * askStorage(申请存储)
     */
    StorageIndex ask(StorageIndex storageIndex, long metaDataLength) throws Exception;

    /**
     * apply（文件落盘）
     */
    void apply(RecoverableTmpFile recoverableTmpFile) throws Exception;

    /**
     * getRecoverableTmpFileName
     */
    String getRecoverableTmpFileName(MetaDataIndex metaDataIndex, String fileExtName) throws Exception;

    /**
     * recoverableTmpFileName（意外恢复使用）
     */
    RecoverableTmpFile getRecoverableTmpFile(String recoverableTmpFileName) throws Exception;
}
