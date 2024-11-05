package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.common.utils.DateUtil;
import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.LruHashMap;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.utils.FileServerUtils;
import cn.bossfriday.im.common.entity.file.MetaData;
import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import cn.bossfriday.im.common.entity.file.RecoverableTmpFile;
import cn.bossfriday.im.common.entity.file.StorageIndex;
import cn.bossfriday.im.common.enums.file.FileStatus;
import cn.bossfriday.im.common.enums.file.OperationResult;
import cn.bossfriday.im.common.enums.file.StorageEngineVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.bossfriday.im.common.constant.FileServerConstant.STORAGE_FILE_CHANNEL_LRU_DURATION;
import static cn.bossfriday.im.common.constant.FileServerConstant.STORAGE_FILE_EXTENSION_NAME;

/**
 * StorageHandler
 *
 * @author chenx
 */
@Slf4j
@CurrentStorageEngineVersion
public class StorageHandler implements IStorageHandler {

    protected final ReentrantReadWriteLock fileChannelLock = new ReentrantReadWriteLock();
    protected LruHashMap<String, FileChannel> storageFileChannelMap = new LruHashMap<>(1000, (key, fileChannel) -> {
        try {
            fileChannel.close();
        } catch (Exception ex) {
            log.warn("FileChannel close failed: " + key);
        }
    }, STORAGE_FILE_CHANNEL_LRU_DURATION);

    @Override
    public StorageIndex getStorageIndex(String namespace) throws IOException {
        int time = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        File storageFile = getStorageFile(namespace, time);
        long offset = storageFile.length();

        return StorageIndex.builder()
                .storeEngineVersion(StorageEngineVersion.V1.getValue())
                .storageNamespace(namespace)
                .time(time)
                .offset(offset)
                .build();
    }

    @Override
    public StorageIndex ask(StorageIndex storageIndex, long dataLength) throws IOException {
        if (storageIndex == null) {
            throw new ServiceRuntimeException("storageIndex is null");
        }

        if (dataLength <= 0) {
            throw new ServiceRuntimeException("dataLength <= 0");
        }

        int currentTime = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        if (storageIndex.getTime() != currentTime) {
            // 如果跨天，则重新初始化 StorageIndex
            storageIndex = this.getStorageIndex(storageIndex.getStorageNamespace());
        }

        storageIndex.addOffset(dataLength);

        return storageIndex.getClonedStorageIndex();
    }

    @SuppressWarnings("squid:S2093")
    @Override
    public Long apply(RecoverableTmpFile recoverableTmpFile) throws IOException {
        if (recoverableTmpFile == null) {
            throw new ServiceRuntimeException("RecoverableTmpFile is null");
        }

        byte[] metaDataBytes = null;
        FileChannel storageFileChannel = null;
        FileChannel tmpFileChannel = null;

        try {
            metaDataBytes = MetaData.builder()
                    .storeEngineVersion(recoverableTmpFile.getStoreEngineVersion())
                    .fileStatus(FileStatus.DEFAULT.getValue())
                    .timestamp(recoverableTmpFile.getTimestamp())
                    .fileName(recoverableTmpFile.getFileName())
                    .fileTotalSize(recoverableTmpFile.getFileTotalSize())
                    .build().serialize();
            long metaIndexHash64 = MetaDataIndex.hash64(recoverableTmpFile.getStorageNamespace(), recoverableTmpFile.getTime(), recoverableTmpFile.getOffset());

            // 存储元数据（不包含临时文件本身）
            storageFileChannel = this.getFileChannel(recoverableTmpFile.getStorageNamespace(), recoverableTmpFile.getTime());
            FileUtil.transferFrom(storageFileChannel, metaDataBytes, recoverableTmpFile.getOffset());

            // 存储临时文件
            long tmpFileDataOffset = recoverableTmpFile.getOffset() + metaDataBytes.length;
            tmpFileChannel = new RandomAccessFile(recoverableTmpFile.getFilePath(), "r").getChannel();
            storageFileChannel.transferFrom(tmpFileChannel, tmpFileDataOffset, tmpFileChannel.size());

            return metaIndexHash64;
        } finally {
            try {
                if (storageFileChannel != null) {
                    storageFileChannel.force(true);
                }

                if (tmpFileChannel != null) {
                    tmpFileChannel.close();
                }

                File tmpFile = new File(recoverableTmpFile.getFilePath());
                Files.delete(tmpFile.toPath());
            } catch (Exception ex) {
                log.error("StorageHandler.apply() finally error!", ex);
            }
        }
    }

    @Override
    public String getRecoverableTmpFileName(RecoverableTmpFile recoverableTmpFile) throws IOException {
        if (Objects.isNull(recoverableTmpFile)) {
            throw new ServiceRuntimeException("recoverableTmpFile is null!");
        }

        recoverableTmpFile.setFilePath("");
        byte[] data = recoverableTmpFile.serialize();
        String extName = FileUtil.getFileExt(recoverableTmpFile.getFileName());

        return Base58Util.encode(data) + "." + extName;
    }

    @Override
    public RecoverableTmpFile getRecoverableTmpFile(String tempDir, String recoverableTmpFileName) throws IOException {
        String fileNameWithoutExtension = FileUtil.getFileNameWithoutExtension(recoverableTmpFileName);
        if (StringUtils.isEmpty(fileNameWithoutExtension)) {
            throw new ServiceRuntimeException("recoverableTmpFileNameWithoutExtension is empty!");
        }

        byte[] data = Base58Util.decode(fileNameWithoutExtension);
        RecoverableTmpFile recoverableTmpFile = new RecoverableTmpFile().deserialize(data);
        recoverableTmpFile.setFilePath(FileUtil.mergeFilePath(tempDir, recoverableTmpFileName));

        return recoverableTmpFile;
    }

    @Override
    public byte[] chunkedDownload(MetaDataIndex metaDataIndex, long fileTotalSize, long offset, int limit) throws IOException {
        if (offset < 0 && limit <= 0) {
            throw new ServiceRuntimeException("invalid position or length: " + offset + "/" + limit);
        }

        if (offset >= fileTotalSize) {
            throw new ServiceRuntimeException("invalid position: position(" + offset + ") >= fileTotalSize(" + fileTotalSize + ")");
        }

        if (offset + limit > fileTotalSize) {
            throw new ServiceRuntimeException("invalid position and length: position(" + offset + ") + length(" + limit + ") > fileTotalSize(" + fileTotalSize + ")");
        }

        FileChannel storageFileChannel = this.getFileChannel(metaDataIndex.getStorageNamespace(), metaDataIndex.getTime());
        long chunkedFileDataBeginOffset = metaDataIndex.getOffset() + metaDataIndex.getMetaDataLength() + offset;

        return FileUtil.transferTo(storageFileChannel, chunkedFileDataBeginOffset, limit, false);
    }

    @Override
    public MetaData getMetaData(MetaDataIndex metaDataIndex) throws IOException {
        FileChannel storageFileChannel = this.getFileChannel(metaDataIndex.getStorageNamespace(), metaDataIndex.getTime());
        byte[] metaDataBytes = FileUtil.transferTo(storageFileChannel, metaDataIndex.getOffset(), metaDataIndex.getMetaDataLength(), false);

        return new MetaData().deserialize(metaDataBytes);
    }

    @Override
    public OperationResult delete(MetaDataIndex metaDataIndex) throws IOException {
        MetaData metaData = this.getMetaData(metaDataIndex);
        // 当前状态为已删除
        if (FileServerUtils.isFileStatusTrue(metaData.getFileStatus(), FileStatus.IS_BIT1)) {
            return OperationResult.OK;
        }

        // 设置状态为删除
        FileChannel storageFileChannel = null;
        try {
            metaData.setFileStatus(FileServerUtils.setFileStatus(metaData.getFileStatus(), FileStatus.IS_BIT1));
            byte[] metaDataBytes = metaData.serialize();
            storageFileChannel = this.getFileChannel(metaDataIndex.getStorageNamespace(), metaDataIndex.getTime());
            FileUtil.transferFrom(storageFileChannel, metaDataBytes, metaDataIndex.getOffset());
        } finally {
            try {
                if (storageFileChannel != null) {
                    storageFileChannel.force(true);
                }
            } catch (Exception ex) {
                log.error("StorageHandler.delete() finally error!", ex);
            }
        }

        return OperationResult.OK;
    }

    /**
     * getFileChannel
     *
     * @param namespace
     * @param time
     * @return
     */
    @SuppressWarnings("squid:S2093")
    protected FileChannel getFileChannel(String namespace, int time) {
        String key = namespace + "-" + time;
        this.fileChannelLock.readLock().lock();
        try {
            if (this.storageFileChannelMap.containsKey(key)) {
                return this.storageFileChannelMap.get(key);
            }
        } finally {
            this.fileChannelLock.readLock().unlock();
        }

        this.fileChannelLock.writeLock().lock();
        try {
            File storageFile = getStorageFile(namespace, time);
            FileChannel fileChannel = new RandomAccessFile(storageFile, "rw").getChannel();
            this.storageFileChannelMap.put(key, fileChannel);

            return fileChannel;
        } catch (Exception ex) {
            log.error("StorageHandler.getFileChannel() error!", ex);
            throw new ServiceRuntimeException("StorageHandler.getFileChannel() error! " + ex.getMessage());
        } finally {
            this.fileChannelLock.writeLock().unlock();
        }
    }

    /**
     * getStorageDayDir
     *
     * @param namespace
     * @param time
     * @return
     */
    protected static File getStorageDayDir(String namespace, int time) {
        File baseDir = StorageEngine.getInstance().getBaseDir();
        File namespaceDir = new File(baseDir, namespace);
        if (!namespaceDir.exists()) {
            synchronized (StorageHandler.class) {
                namespaceDir.mkdirs();
            }
        }

        File dayDir = new File(namespaceDir, String.valueOf(time));
        if (!dayDir.exists()) {
            synchronized (StorageHandler.class) {
                dayDir.mkdirs();
            }
        }

        return dayDir;
    }

    /**
     * getStorageFile
     *
     * @param namespace
     * @param time
     * @return
     * @throws IOException
     */
    protected static File getStorageFile(String namespace, int time) throws IOException {
        File dayDir = getStorageDayDir(namespace, time);
        String storageFileName = time + "." + STORAGE_FILE_EXTENSION_NAME;
        File storageFile = new File(dayDir, storageFileName);
        if (!storageFile.exists()) {
            synchronized (StorageHandler.class) {
                if (!storageFile.createNewFile()) {
                    throw new ServiceRuntimeException("Create storage file failed! namespace:" + namespace + ", time:" + time);
                }
            }
        }

        return storageFile;
    }
}
