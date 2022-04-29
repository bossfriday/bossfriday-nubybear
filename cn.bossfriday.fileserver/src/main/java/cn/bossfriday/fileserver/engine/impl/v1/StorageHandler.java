package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.*;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.bossfriday.fileserver.common.FileServerConst.DOWNLOAD_CHUNK_SIZE;
import static cn.bossfriday.fileserver.common.FileServerConst.STORAGE_FILE_EXTENSION_NAME;

@Slf4j
@CurrentStorageEngineVersion
public class StorageHandler implements IStorageHandler {

    private final ReentrantReadWriteLock fileChannelLock = new ReentrantReadWriteLock();
    private LRUHashMap<String, FileChannel> storageFileChannelMap = new LRUHashMap<>(1000, new F.Action2<String, FileChannel>() {

        @Override
        public void invoke(String key, FileChannel fileChannel) {
            try {
                fileChannel.close();
            } catch (Exception ex) {
                log.warn("FileChannel close failed: " + key);
            }
        }
    }, 1000 * 60 * 60L * 8);

    private LRUHashMap<MetaDataIndex, MetaData> metaDataMap = new LRUHashMap<>(10000, null, 1000 * 60 * 60L * 8);

    @Override
    public StorageIndex getStorageIndex(String namespace) throws Exception {
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
    public StorageIndex ask(StorageIndex storageIndex, long dataLength) throws Exception {
        if (storageIndex == null)
            throw new BizException("storageIndex is null");

        if (dataLength <= 0)
            throw new BizException("dataLength <= 0");

        int currentTime = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        if (storageIndex.getTime() != currentTime) {
            // 如果跨天，则重新初始化 StorageIndex
            storageIndex = getStorageIndex(storageIndex.getNamespace());
        }

        storageIndex.addOffset(dataLength);

        return storageIndex.clone();
    }

    @Override
    public void apply(RecoverableTmpFile recoverableTmpFile) throws Exception {
        byte[] metaDataBytes = null;
        FileChannel storageFileChannel = null;
        FileChannel tmpFileChannel = null;

        try {
            if (recoverableTmpFile == null)
                throw new BizException("RecoverableTmpFile is null");

            metaDataBytes = MetaData.builder()
                    .storeEngineVersion(recoverableTmpFile.getStoreEngineVersion())
                    .fileStatus(FileStatus.Normal.getValue())
                    .timestamp(recoverableTmpFile.getTimestamp())
                    .fileName(recoverableTmpFile.getFileName())
                    .fileTotalSize(recoverableTmpFile.getFileTotalSize())
                    .build().serialize();

            // 存储元数据（不包含临时文件本身）
            storageFileChannel = getFileChannel(recoverableTmpFile.getNamespace(), recoverableTmpFile.getTime());
            FileUtil.transferFrom(storageFileChannel, metaDataBytes, recoverableTmpFile.getOffset());

            // 存储临时文件
            long tmpFileDataOffset = recoverableTmpFile.getOffset() + metaDataBytes.length;
            tmpFileChannel = new RandomAccessFile(recoverableTmpFile.getFilePath(), "r").getChannel();
            storageFileChannel.transferFrom(tmpFileChannel, tmpFileDataOffset, tmpFileChannel.size());
            log.info("RecoverableTmpFile apply done: " + recoverableTmpFile.getFileTransactionId());
        } finally {
            try {
                if (storageFileChannel != null) {
                    storageFileChannel.force(true);
                }

                if (tmpFileChannel != null) {
                    tmpFileChannel.close();
                }

                File tmpFile = new File(recoverableTmpFile.getFilePath());
                if (!tmpFile.delete()) {
                    throw new BizException("delete recoverableTmpFile failed: " + recoverableTmpFile.getFileTransactionId());
                }
            } catch (Exception ex) {
                log.error("apply finally error!", ex);
            } finally {
                metaDataBytes = null;
                recoverableTmpFile = null;
            }
        }
    }

    @Override
    public String getRecoverableTmpFileName(MetaDataIndex metaDataIndex, String fileExtName) throws Exception {
        return Base58Util.encode(metaDataIndex.serialize()) + "." + fileExtName;
    }

    @Override
    public RecoverableTmpFile getRecoverableTmpFile(String recoverableTmpFileName) throws Exception {
        // todo 服务重启临时文件落盘恢复
        return null;
    }

    @Override
    public byte[] chunkedDownload(String fileTransactionId, MetaDataIndex metaDataIndex, long chunkIndex) throws Exception {
        FileChannel storageFileChannel = getFileChannel(metaDataIndex.getNamespace(), metaDataIndex.getTime());
        FileTransactionContext fileCtx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
        if (fileCtx == null) {
            throw new BizException("FileTransactionContext not existed: " + fileTransactionId);
        }

        // 第一个分片下载
        if (chunkIndex == 0) {
            MetaData metaData = getMetaData(storageFileChannel, metaDataIndex);
            long fileSize = metaData.getFileTotalSize();
            int chunkSize = DOWNLOAD_CHUNK_SIZE;
            long chunkCount = fileSize % chunkSize == 0 ? (long) (fileSize / chunkSize) : (long) (fileSize / chunkSize + 1);
            fileCtx.setChunkCount(chunkCount);
            fileCtx.setMetaData(metaData);
        }

        if (fileCtx.getChunkCount() <= 0)
            throw new BizException("fileCtx.getChunkCount()<=0: " + fileTransactionId);

        if (fileCtx.getMetaData() == null)
            throw new BizException("fileCtx.getMetaData() == null: " + fileTransactionId);

        long chunkedFileDataBeginOffset = getChunkedFileDataBeginOffset(metaDataIndex.getOffset(), metaDataIndex.getMetaDataLength(), chunkIndex, fileCtx.getChunkCount());
        long chunkedFileDataEndOffset = getChunkedFileDataEndOffset(metaDataIndex.getOffset(), metaDataIndex.getMetaDataLength(), chunkIndex, fileCtx.getChunkCount(), fileCtx.getMetaData().getFileTotalSize());
        long currentChunkSize = chunkedFileDataEndOffset - chunkedFileDataBeginOffset + 1;

        return FileUtil.transferTo(storageFileChannel, chunkedFileDataBeginOffset, currentChunkSize, false);
    }

    @Override
    public MetaData getMetaData(String fileTransactionId, MetaDataIndex metaDataIndex) throws Exception {
        FileChannel storageFileChannel = getFileChannel(metaDataIndex.getNamespace(), metaDataIndex.getTime());
        return getMetaData(storageFileChannel, metaDataIndex);
    }

    /**
     * getFileChannel
     */
    private FileChannel getFileChannel(String namespace, int time) throws Exception {
        String key = namespace + "-" + time;
        fileChannelLock.readLock().lock();
        try {
            if (storageFileChannelMap.containsKey(key))
                return storageFileChannelMap.get(key);
        } finally {
            fileChannelLock.readLock().unlock();
        }

        fileChannelLock.writeLock().lock();
        try {
            File storageFile = getStorageFile(namespace, time);
            FileChannel fileChannel = new RandomAccessFile(storageFile, "rw").getChannel();
            storageFileChannelMap.put(key, fileChannel);

            return fileChannel;
        } finally {
            fileChannelLock.writeLock().unlock();
        }
    }

    /**
     * getStorageDayDir
     */
    private static File getStorageDayDir(String namespace, int time) throws Exception {
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
     */
    private static File getStorageFile(String namespace, int time) throws Exception {
        File dayDir = getStorageDayDir(namespace, time);
        String storageFileName = String.valueOf(time) + "." + STORAGE_FILE_EXTENSION_NAME;
        File storageFile = new File(dayDir, storageFileName);
        if (!storageFile.exists()) {
            synchronized (StorageHandler.class) {
                storageFile.createNewFile();
            }
        }

        return storageFile;
    }

    /**
     * getChunkedFileDataBeginOffset
     */
    private static long getChunkedFileDataBeginOffset(long metaDataOffset, long metaDataLength, long chunkIndex, long chunkCount) throws Exception {
        if (chunkIndex + 1 > chunkCount)
            throw new BizException("invalid chunkIndex: chunkIndex + 1 > chunkCount");

        return metaDataOffset + metaDataLength + (chunkIndex * DOWNLOAD_CHUNK_SIZE);
    }

    /**
     * getChunkedFileDataEndOffset
     */
    private static long getChunkedFileDataEndOffset(long metaDataOffset, long metaDataLength, long chunkIndex, long chunkCount, long fileTotalSize) throws Exception {
        if (chunkIndex + 1 > chunkCount)
            throw new BizException("invalid chunkIndex: chunkIndex + 1 > chunkCount");

        if (chunkIndex + 1 < chunkCount) {
            return metaDataOffset + metaDataLength + ((chunkIndex + 1) * DOWNLOAD_CHUNK_SIZE) - 1;
        } else {
            long x = chunkCount * DOWNLOAD_CHUNK_SIZE - fileTotalSize;
            if (x < 0)
                throw new BizException("invalid chunkCount: (chunkCount * DOWNLOAD_CHUNK_SIZE - fileTotalSize) < 0");

            long lastChunkSize = DOWNLOAD_CHUNK_SIZE - x;
            return metaDataOffset + metaDataLength + (chunkIndex * DOWNLOAD_CHUNK_SIZE) + lastChunkSize - 1;
        }
    }

    /**
     * getMetaData
     * 不加锁原因：1：MetaData只读不写；2：同一个MetaData在并发下可能导致的重复反序列化对整个过程无影响；
     *
     * @param storageFileChannel
     * @param metaDataIndex
     * @return
     * @throws Exception
     */
    private MetaData getMetaData(FileChannel storageFileChannel, MetaDataIndex metaDataIndex) throws Exception {
        if (metaDataMap.containsKey(metaDataIndex))
            return metaDataMap.get(metaDataIndex);

        byte[] metaDataBytes = null;
        try {
            metaDataBytes = FileUtil.transferTo(storageFileChannel, metaDataIndex.getOffset(), metaDataIndex.getMetaDataLength(), false);
            MetaData metaData = new MetaData().deserialize(metaDataBytes);
            metaDataMap.putIfAbsent(metaDataIndex, metaData);   // 由于没有加锁，因此这里用putIfAbsent。

            return metaData;
        } finally {
            metaDataBytes = null;
        }
    }
}
