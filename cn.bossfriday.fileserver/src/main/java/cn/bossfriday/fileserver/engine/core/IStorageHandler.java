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
     * chunkedDownload（分片下载）
     *
     * @param metaDataIndex
     * @param chunkIndex：从0开始
     * @return
     * @throws Exception
     */
    byte[] chunkedDownload(String fileTransactionId, MetaDataIndex metaDataIndex, long chunkIndex) throws Exception;

    /**
     * getMetaData
     *
     * @param fileTransactionId
     * @param metaDataIndex
     * @return
     * @throws Exception
     */
    MetaData getMetaData(String fileTransactionId, MetaDataIndex metaDataIndex) throws Exception;
}
