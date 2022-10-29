package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.*;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.entity.MetaData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.engine.entity.RecoverableTmpFile;
import cn.bossfriday.fileserver.engine.entity.StorageIndex;
import cn.bossfriday.fileserver.engine.enums.FileStatus;
import cn.bossfriday.fileserver.engine.enums.StorageEngineVersion;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.bossfriday.fileserver.common.FileServerConst.STORAGE_FILE_EXTENSION_NAME;

/**
 * StorageHandler
 *
 * @author chenx
 */
@Slf4j
@CurrentStorageEngineVersion
public class StorageHandler implements IStorageHandler {

    protected final ReentrantReadWriteLock fileChannelLock = new ReentrantReadWriteLock();
    protected LruHashMap<String, FileChannel> storageFileChannelMap = new LruHashMap<>(1000, new Func.Action2<String, FileChannel>() {

        @Override
        public void invoke(String key, FileChannel fileChannel) {
            try {
                fileChannel.close();
            } catch (Exception ex) {
                log.warn("FileChannel close failed: " + key);
            }
        }
    }, 1000 * 60 * 60L * 8);

    @Override
    public StorageIndex getStorageIndex(String namespace) throws IOException {
        int time = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        File storageFile = getStorageFile(namespace, time);
        long offset = storageFile.length();

        return StorageIndex.builder()
                .storeEngineVersion(StorageEngineVersion.V1.getValue())
                .namespace(namespace)
                .time(time)
                .offset(offset)
                .build();
    }

    @Override
    public StorageIndex ask(StorageIndex storageIndex, long dataLength) throws IOException {
        if (storageIndex == null) {
            throw new BizException("storageIndex is null");
        }

        if (dataLength <= 0) {
            throw new BizException("dataLength <= 0");
        }

        int currentTime = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        if (storageIndex.getTime() != currentTime) {
            // 如果跨天，则重新初始化 StorageIndex
            storageIndex = this.getStorageIndex(storageIndex.getNamespace());
        }

        storageIndex.addOffset(dataLength);

        return storageIndex.clone();
    }

    @Override
    public Long apply(RecoverableTmpFile recoverableTmpFile) throws IOException {
        if (recoverableTmpFile == null) {
            throw new BizException("RecoverableTmpFile is null");
        }

        byte[] metaDataBytes = null;
        FileChannel storageFileChannel = null;
        FileChannel tmpFileChannel = null;

        try {
            metaDataBytes = MetaData.builder()
                    .storeEngineVersion(recoverableTmpFile.getStoreEngineVersion())
                    .fileStatus(FileStatus.NORMAL.getValue())
                    .timestamp(recoverableTmpFile.getTimestamp())
                    .fileName(recoverableTmpFile.getFileName())
                    .fileTotalSize(recoverableTmpFile.getFileTotalSize())
                    .build().serialize();
            long metaIndexHash64 = MetaDataIndex.hash64(recoverableTmpFile.getNamespace(), recoverableTmpFile.getTime(), recoverableTmpFile.getOffset());

            // 存储元数据（不包含临时文件本身）
            storageFileChannel = this.getFileChannel(recoverableTmpFile.getNamespace(), recoverableTmpFile.getTime());
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
                log.error("apply finally error!", ex);
            }
        }
    }

    @Override
    public String getRecoverableTmpFileName(MetaDataIndex metaDataIndex) throws IOException {
        return Base58Util.encode(metaDataIndex.serialize()) + "." + metaDataIndex.getFileExtName();
    }

    @Override
    public RecoverableTmpFile getRecoverableTmpFile(String recoverableTmpFileName) throws IOException {
        // TODO: 服务重启临时文件落盘恢复
        return null;
    }

    @Override
    public byte[] chunkedDownload(MetaDataIndex metaDataIndex, long fileTotalSize, long position, int length) throws IOException {
        if (position < 0 && length <= 0) {
            throw new BizException("invalid position or length: " + position + "/" + length);
        }

        if (position >= fileTotalSize) {
            throw new BizException("invalid position: position(" + position + ") >= fileTotalSize(" + fileTotalSize + ")");
        }

        if (position + length > fileTotalSize) {
            throw new BizException("invalid position and length: position(" + position + ") + length(" + length + ") > fileTotalSize(" + fileTotalSize + ")");
        }

        FileChannel storageFileChannel = this.getFileChannel(metaDataIndex.getNamespace(), metaDataIndex.getTime());
        long chunkedFileDataBeginOffset = metaDataIndex.getOffset() + metaDataIndex.getMetaDataLength() + position;

        return FileUtil.transferTo(storageFileChannel, chunkedFileDataBeginOffset, length, false);
    }

    @Override
    public MetaData getMetaData(MetaDataIndex metaDataIndex) throws IOException {
        FileChannel storageFileChannel = this.getFileChannel(metaDataIndex.getNamespace(), metaDataIndex.getTime());
        byte[] metaDataBytes = FileUtil.transferTo(storageFileChannel, metaDataIndex.getOffset(), metaDataIndex.getMetaDataLength(), false);

        return new MetaData().deserialize(metaDataBytes);
    }

    /**
     * getFileChannel
     *
     * @param namespace
     * @param time
     * @return
     */
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
            throw new BizException("StorageHandler.getFileChannel() error! " + ex.getMessage());
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
                    throw new BizException("Create storage file failed! namespace:" + namespace + ", time:" + time);
                }
            }
        }

        return storageFile;
    }
}
