package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.model.MetaData;
import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import cn.bossfriday.fileserver.engine.model.RecoverableTmpFile;
import cn.bossfriday.fileserver.engine.model.StorageIndex;

import java.io.IOException;

/**
 * IStorageHandler
 *
 * @author chenx
 */
public interface IStorageHandler {

    /**
     * getStorageIndex
     *
     * @param namespace
     * @return
     * @throws IOException
     */
    StorageIndex getStorageIndex(String namespace) throws IOException;

    /**
     * askStorage(申请存储)
     *
     * @param storageIndex
     * @param metaDataLength
     * @return
     * @throws IOException
     */
    StorageIndex ask(StorageIndex storageIndex, long metaDataLength) throws IOException;

    /**
     * apply（文件落盘）
     *
     * @param recoverableTmpFile
     * @return
     * @throws IOException
     */
    Long apply(RecoverableTmpFile recoverableTmpFile) throws IOException;

    /**
     * getRecoverableTmpFileName（获取可直接落盘/恢复临时文件）
     *
     * @param metaDataIndex
     * @return
     * @throws IOException
     */
    String getRecoverableTmpFileName(MetaDataIndex metaDataIndex) throws IOException;

    /**
     * recoverableTmpFileName（意外恢复使用）
     *
     * @param recoverableTmpFileName
     * @return
     * @throws IOException
     */
    RecoverableTmpFile getRecoverableTmpFile(String recoverableTmpFileName) throws IOException;

    /**
     * chunkedDownload
     *
     * @param metaDataIndex
     * @param fileTotalSize
     * @param offset
     * @param limit
     * @return
     * @throws IOException
     */
    byte[] chunkedDownload(MetaDataIndex metaDataIndex, long fileTotalSize, long offset, int limit) throws IOException;

    /**
     * getMetaData
     *
     * @param metaDataIndex
     * @return
     * @throws IOException
     */
    MetaData getMetaData(MetaDataIndex metaDataIndex) throws IOException;
}
