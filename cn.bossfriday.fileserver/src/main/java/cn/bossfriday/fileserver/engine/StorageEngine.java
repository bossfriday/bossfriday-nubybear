package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.fileserver.engine.core.BaseStorageEngine;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.im.common.conf.SystemConfigLoader;
import cn.bossfriday.im.common.entity.conf.FileServerConfig;
import cn.bossfriday.im.common.entity.conf.StorageNamespace;
import cn.bossfriday.im.common.entity.file.*;
import cn.bossfriday.im.common.enums.file.OperationResult;
import cn.bossfriday.im.common.enums.file.StorageEngineVersion;
import cn.bossfriday.im.common.message.file.WriteTmpFileOutput;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.bossfriday.im.common.constant.FileServerConstant.*;

/**
 * StorageEngine
 *
 * @author chenx
 */
@Slf4j
@SuppressWarnings("squid:S6548")
public class StorageEngine extends BaseStorageEngine {

    private static final int RECOVERABLE_TMP_FILE_WARNING_THRESHOLD = 1024 * 5;
    private static final String META_DATA_INDEX_IS_NULL = "MetaDataIndex is null!";
    private static final String META_DATA_IS_NULL = "MetaData is null!";

    private ConcurrentHashMap<String, StorageIndex> storageIndexMap;
    private ConcurrentHashMap<Long, RecoverableTmpFile> recoverableTmpFileHashMap = new ConcurrentHashMap<>();
    private StorageCleaner storageCleaner;

    @Getter
    private File baseDir;

    @Getter
    private File tmpDir;

    @Getter
    private HashMap<String, StorageNamespace> namespaceMap;

    private StorageEngine() {
        super(RECOVERABLE_TMP_FILE_WARNING_THRESHOLD * 2);
        this.init();
    }

    /**
     * getInstance
     */
    public static StorageEngine getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    protected void startup() {
        try {
            // 未落盘临时文件异常恢复
            this.recoverTmpFile();

            // 加载存储指针
            this.loadStorageIndex();

            // 过期文件自动清理
            this.storageCleaner.startup();
        } catch (Exception ex) {
            log.error("StorageEngine.startup() error!", ex);
        }
    }

    @Override
    protected void shutdown() {
        try {
            this.recoverTmpFile();
            this.storageCleaner.shutdown();
        } catch (Exception ex) {
            log.error("StorageEngine.shutdown() error!", ex);
        }
    }

    @Override
    protected void onRecoverableTmpFileEvent(RecoverableTmpFile event) {
        String fileTransactionId = event.getFileTransactionId();
        try {
            // 存储文件落盘（Disruptor保障先进先出）
            IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(event.getStoreEngineVersion());
            long metaDataIndexHash64 = storageHandler.apply(event);
            this.recoverableTmpFileHashMap.remove(metaDataIndexHash64);
            log.info("RecoverableTmpFile apply done, fileTransactionId: " + fileTransactionId + ", offset:" + event.getOffset());
        } catch (Exception ex) {
            log.error("onRecoverableTmpFileEvent() error!" + fileTransactionId, ex);
        }
    }

    /**
     * upload 文件上传
     *
     * @param data
     * @return
     * @throws IOException
     */
    public synchronized MetaDataIndex upload(WriteTmpFileOutput data) throws IOException {
        if (data == null) {
            throw new ServiceRuntimeException("WriteTmpFileResult is null!");
        }

        // 申请存储空间
        String fileTransactionId = data.getFileTransactionId();
        int engineVersion = data.getStorageEngineVersion();
        IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(engineVersion);
        IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(engineVersion);
        ITmpFileHandler tmpFileHandler = StorageHandlerFactory.getTmpFileHandler(engineVersion);
        long metaDataTotalLength = metaDataHandler.getMetaDataTotalLength(data.getFileName(), data.getFileTotalSize());
        int metaDataLength = metaDataHandler.getMetaDataLength(data.getFileName());

        StorageIndex currentStorageIndex = this.getStorageIndex(data.getStorageNamespace(), engineVersion);
        StorageIndex resultIndex = storageHandler.ask(currentStorageIndex, metaDataTotalLength);

        if (resultIndex == null) {
            throw new ServiceRuntimeException("Result StorageIndex is null: " + data.getFileTransactionId());
        }

        long metaDataIndexOffset = resultIndex.getOffset() - metaDataTotalLength;
        if (metaDataIndexOffset < 0) {
            throw new ServiceRuntimeException("metaDataIndexOffset <0: " + data.getFileTransactionId());
        }

        MetaDataIndex metaDataIndex = MetaDataIndex.builder()
                .clusterNode(data.getClusterNodeName())
                .storeEngineVersion(engineVersion)
                .storageNamespace(data.getStorageNamespace())
                .time(resultIndex.getTime())
                .offset(metaDataIndexOffset)
                .metaDataLength(metaDataLength)
                .fileExtName(data.getFileExtName())
                .build();

        // 构建临时文件
        RecoverableTmpFile recoverableTmpFile = RecoverableTmpFile.builder()
                .fileTransactionId(fileTransactionId)
                .storeEngineVersion(data.getStorageEngineVersion())
                .storageNamespace(data.getStorageNamespace())
                .time(resultIndex.getTime())
                .offset(metaDataIndex.getOffset())
                .timestamp(data.getTimestamp())
                .fileName(data.getFileName())
                .fileTotalSize(data.getFileTotalSize())
                .build();
        String recoverableTmpFileName = storageHandler.getRecoverableTmpFileName(recoverableTmpFile);
        String recoverableTmpFilePath = tmpFileHandler.rename(data.getFilePath(), recoverableTmpFileName);
        recoverableTmpFile.setFilePath(recoverableTmpFilePath);

        // 入队落盘
        this.enqueue(metaDataIndex.hash64(), recoverableTmpFile);
        log.info("StorageEngine.upload() done:" + recoverableTmpFile);

        return metaDataIndex;
    }

    /**
     * chunkedDownload 文件分片下载
     *
     * @param metaDataIndex
     * @param metaData
     * @param offset
     * @param limit
     * @return
     * @throws IOException
     */
    public ChunkedMetaData chunkedDownload(MetaDataIndex metaDataIndex, MetaData metaData, long offset, int limit) throws IOException {
        if (metaDataIndex == null) {
            throw new ServiceRuntimeException(META_DATA_INDEX_IS_NULL);
        }

        if (metaData == null) {
            throw new ServiceRuntimeException(META_DATA_IS_NULL);
        }

        // 临时文件落盘采用零拷贝+顺序写盘方式非常高效，因此这里采用自旋等待的无锁方式
        for (int i = 0; ; i++) {
            if (!this.recoverableTmpFileHashMap.containsKey(metaDataIndex.hash64())) {
                break;
            }

            if (this.recoverableTmpFileHashMap.size() > RECOVERABLE_TMP_FILE_WARNING_THRESHOLD) {
                // 这种情况经常发生则建议横向扩容（先hardCode，可以考虑做成配置及对接业务监控等）
                log.error("StorageEngine.recoverableTmpFileHashMap.size() > RECOVERABLE_TMP_FILE_WARNING_THRESHOLD! (" + this.recoverableTmpFileHashMap.size() + ")");
            }

            // 如果临时文件没有落盘则开始自旋
            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {
                log.error("StorageEngine.chunkedDownload() spinning error!", e);
                Thread.currentThread().interrupt();
            }
        }

        int version = metaDataIndex.getStoreEngineVersion();
        IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(version);
        byte[] chunkedData = storageHandler.chunkedDownload(metaDataIndex, metaData.getFileTotalSize(), offset, limit);

        return ChunkedMetaData.builder()
                .offset(offset)
                .chunkedData(chunkedData)
                .build();
    }

    /**
     * getMetaData
     *
     * @param metaDataIndex
     * @return
     * @throws IOException
     */
    public MetaData getMetaData(MetaDataIndex metaDataIndex) throws IOException {
        if (metaDataIndex == null) {
            throw new ServiceRuntimeException(META_DATA_INDEX_IS_NULL);
        }

        int version = metaDataIndex.getStoreEngineVersion();
        IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(version);

        return storageHandler.getMetaData(metaDataIndex);
    }

    /**
     * 文件删除
     *
     * @param metaDataIndex
     * @return
     * @throws IOException
     */
    public OperationResult delete(MetaDataIndex metaDataIndex) throws IOException {
        if (metaDataIndex == null) {
            throw new ServiceRuntimeException(META_DATA_INDEX_IS_NULL);
        }

        return StorageHandlerFactory.getStorageHandler(metaDataIndex.getStoreEngineVersion()).delete(metaDataIndex);
    }

    /**
     * init
     */
    private void init() {
        try {
            FileServerConfig config = SystemConfigLoader.getInstance().getFileServerConfig();
            this.storageCleaner = new StorageCleaner(config);

            // 存储空间
            this.namespaceMap = new HashMap<>(16);
            config.getNamespaces().forEach(item -> {
                String key = item.getName().toLowerCase().trim();
                if (!this.namespaceMap.containsKey(item.getName())) {
                    this.namespaceMap.put(key, item);
                }
            });

            // 目录初始化
            this.baseDir = new File(config.getStorageRootPath(), SystemConfigLoader.getInstance().getSystemConfig().getClusterNode().getName());
            if (!this.baseDir.exists()) {
                this.baseDir.mkdirs();
            }

            for (String spaceName : this.namespaceMap.keySet()) {
                File storageNamespaceDir = new File(this.baseDir, spaceName);
                if (!storageNamespaceDir.exists()) {
                    storageNamespaceDir.mkdirs();
                }
            }

            this.tmpDir = new File(this.baseDir, FILE_PATH_TMP);
            if (!this.tmpDir.exists()) {
                this.tmpDir.mkdirs();
            }
        } catch (Exception e) {
            log.error("StorageEngine.init() error!", e);
        }
    }

    /**
     * loadStorageIndex
     *
     * @throws IOException
     */
    private void loadStorageIndex() throws IOException {
        this.storageIndexMap = new ConcurrentHashMap<>(16);
        StorageEngineVersion[] versions = StorageEngineVersion.class.getEnumConstants();
        for (String namespace : this.namespaceMap.keySet()) {
            for (StorageEngineVersion item : versions) {
                int version = item.getValue();
                IStorageHandler handler = StorageHandlerFactory.getStorageHandler(version);
                StorageIndex storageIndex = handler.getStorageIndex(namespace);
                this.storageIndexMap.put(getStorageIndexMapKey(namespace, version), storageIndex);
                log.info(storageIndex.toString());
            }
        }
    }

    /**
     * 未落盘临时文件异常恢复
     * <p>
     * 服务非正常停止可能导致临时文件未落盘，由于写盘采用零拷贝顺序写，因此实际中几乎撞不到。
     */
    private void recoverTmpFile() {
        IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(DEFAULT_STORAGE_ENGINE_VERSION);
        File[] tmpFiles = this.tmpDir.listFiles();
        List<RecoverableTmpFile> recoverableTmpFiles = new ArrayList<>();
        for (File tmpFile : tmpFiles) {
            try {
                String tmpFileName = tmpFile.getName();
                String tmpFileExtName = FileUtil.getFileExt(tmpFileName);

                // 删除未完成不可恢复ing文件
                if (FILE_UPLOADING_TMP_FILE_EXT.equalsIgnoreCase(tmpFileExtName)) {
                    Files.delete(tmpFile.toPath());
                    log.info("Delete ing temp file done, {}", tmpFileName);

                    continue;
                }

                RecoverableTmpFile recoverableTmpFile = storageHandler.getRecoverableTmpFile(this.tmpDir.getAbsolutePath(), tmpFileName);
                recoverableTmpFiles.add(recoverableTmpFile);
            } catch (Exception ex) {
                log.error("StorageEngine.recoverTmpFile() error!", ex);
            }
        }

        if (CollectionUtils.isEmpty(recoverableTmpFiles)) {
            log.info("StorageEngine.recoverTmpFile() done, no RecoverableTmpFile need to recover.");
            return;
        }

        // 排序后（java无法直接向后跳写文件）入队落盘
        Collections.sort(recoverableTmpFiles);
        for (RecoverableTmpFile recoverableTmpFile : recoverableTmpFiles) {
            long hash = MetaDataIndex.hash64(recoverableTmpFile.getStorageNamespace(), recoverableTmpFile.getTime(), recoverableTmpFile.getOffset());
            this.enqueue(hash, recoverableTmpFile);
        }

        log.info("StorageEngine.recoverTmpFile() done.");
    }

    /**
     * getStorageIndexMapKey
     *
     * @param namespace
     * @param storageEngineVersion
     * @return
     */
    private static String getStorageIndexMapKey(String namespace, int storageEngineVersion) {
        return namespace + "-" + storageEngineVersion;
    }

    /**
     * getStorageIndex
     *
     * @param namespace
     * @param storageEngineVersion
     * @return
     */
    private StorageIndex getStorageIndex(String namespace, int storageEngineVersion) {
        String key = getStorageIndexMapKey(namespace, storageEngineVersion);
        if (!this.storageIndexMap.containsKey(key)) {
            throw new ServiceRuntimeException("StorageIndex not existed: " + key);
        }

        return this.storageIndexMap.get(key);
    }

    /**
     * enqueue
     *
     * @param hash
     * @param recoverableTmpFile
     */
    private void enqueue(long hash, RecoverableTmpFile recoverableTmpFile) {
        this.recoverableTmpFileHashMap.put(hash, recoverableTmpFile);
        this.publishEvent(recoverableTmpFile);
    }

    /**
     * SingletonHolder
     */
    private static class SingletonHolder {
        private static final StorageEngine INSTANCE = new StorageEngine();
    }
}
