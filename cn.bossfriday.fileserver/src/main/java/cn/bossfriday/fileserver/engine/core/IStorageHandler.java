package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.MetaData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.engine.entity.RecoverableTmpFile;
import cn.bossfriday.fileserver.engine.entity.StorageIndex;

public interface IStorageHandler {
    /**
     * getStorageIndex
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    StorageIndex getStorageIndex(String namespace) throws Exception;

    /**
     * askStorage(申请存储)
     *
     * @param storageIndex
     * @param metaDataLength
     * @return
     * @throws Exception
     */
    StorageIndex ask(StorageIndex storageIndex, long metaDataLength) throws Exception;

    /**
     * apply（文件落盘）
     *
     * @param recoverableTmpFile
     * @throws Exception
     */
    void apply(RecoverableTmpFile recoverableTmpFile) throws Exception;

    /**
     * getRecoverableTmpFileName（获取可直接落盘/恢复临时文件）
     *
     * @param metaDataIndex
     * @param fileExtName
     * @return
     * @throws Exception
     */
    String getRecoverableTmpFileName(MetaDataIndex metaDataIndex, String fileExtName) throws Exception;

    /**
     * recoverableTmpFileName（意外恢复使用）
     *
     * @param recoverableTmpFileName
     * @return
     * @throws Exception
     */
    RecoverableTmpFile getRecoverableTmpFile(String recoverableTmpFileName) throws Exception;

    /**
     * chunkedDownload
     *
     * @param metaDataIndex
     * @param fileTotalSize
     * @param position
     * @param length
     * @return
     * @throws Exception
     */
    byte[] chunkedDownload(MetaDataIndex metaDataIndex, long fileTotalSize, long position, int length) throws Exception;

    /**
     * getMetaData
     *
     * @param metaDataIndex
     * @return
     * @throws Exception
     */
    MetaData getMetaData(MetaDataIndex metaDataIndex) throws Exception;
}
