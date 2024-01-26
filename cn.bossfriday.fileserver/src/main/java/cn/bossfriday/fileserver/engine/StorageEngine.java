package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileResult;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.common.conf.StorageNamespace;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.core.BaseStorageEngine;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.enums.StorageEngineVersion;
import cn.bossfriday.fileserver.engine.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static cn.bossfriday.fileserver.common.FileServerConst.FILE_PATH_TMP;

/**
 * StorageDispatcher
 *
 * @author chenx
 */
@Slf4j
public class StorageEngine extends BaseStorageEngine {

    private static volatile StorageEngine instance = null;
    private static final int RECOVERABLE_TMP_FILE_WARNING_THRESHOLD = 10000;

    private ConcurrentHashMap<String, StorageIndex> storageIndexMap;
    private ConcurrentHashMap<Long, RecoverableTmpFile> recoverableTmpFileHashMap = new ConcurrentHashMap<>();

    @Getter
    private File baseDir;

    @Getter
    private File tmpDir;

    @Getter
    private HashMap<String, StorageNamespace> namespaceMap;

    private StorageEngine() {
        super(128 * 1024);
        this.init();
    }

    /**
     * getInstance
     */
    public static StorageEngine getInstance() {
        if (instance == null) {
            synchronized (StorageEngine.class) {
                if (instance == null) {
                    instance = new StorageEngine();
                }
            }
        }

        return instance;
    }

    @Override
    protected void startup() {
        try {
            // 过期文件自动清理
            this.cleanupExpiredFiles();

            // 服务非正常停止可能导致RecoverableTmpFile未落盘
            this.loadRecoverableTmpFile();

            // 加载存储指针
            this.loadStorageIndex();
        } catch (Exception ex) {
            log.error("StorageEngine.startup() error!", ex);
        }
    }

    @Override
    protected void shutdown() {
        try {
            this.loadRecoverableTmpFile();
        } catch (Exception ex) {
            log.error("StorageEngine.shutdown() error!", ex);
        }
    }

    @Override
    protected void onRecoverableTmpFileEvent(RecoverableTmpFile event) {
        String fileTransactionId = event.getFileTransactionId();
        try {
            IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(event.getStoreEngineVersion());
            long metaDataIndexHash64 = storageHandler.apply(event);
            this.recoverableTmpFileHashMap.remove(metaDataIndexHash64);
            log.info("RecoverableTmpFile apply done: " + fileTransactionId + ",offset:" + event.getOffset());
        } catch (Exception ex) {
            log.error("onRecoverableTmpFileEvent() error!" + fileTransactionId, ex);
        }
    }

    /**
     * upload 文件上传
     * synchronized：为了保障落盘为顺序写盘
     *
     * @param data
     * @return
     * @throws IOException
     */
    public synchronized MetaDataIndex upload(WriteTmpFileResult data) throws IOException {
        if (data == null) {
            throw new ServiceRuntimeException("WriteTmpFileResult is null!");
        }

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

        String recoverableTmpFileName = storageHandler.getRecoverableTmpFileName(metaDataIndex);
        String recoverableTmpFilePath = tmpFileHandler.rename(data.getFilePath(), recoverableTmpFileName);
        RecoverableTmpFile recoverableTmpFile = RecoverableTmpFile.builder()
                .fileTransactionId(fileTransactionId)
                .storeEngineVersion(data.getStorageEngineVersion())
                .storageNamespace(data.getStorageNamespace())
                .time(resultIndex.getTime())
                .offset(metaDataIndex.getOffset())
                .timestamp(data.getTimestamp())
                .fileName(data.getFileName())
                .fileTotalSize(data.getFileTotalSize())
                .filePath(recoverableTmpFilePath)
                .build();

        this.recoverableTmpFileHashMap.put(metaDataIndex.hash64(), recoverableTmpFile);
        this.publishEvent(recoverableTmpFile);
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
            throw new ServiceRuntimeException("MetaDataIndex is null!");
        }

        if (metaData == null) {
            throw new ServiceRuntimeException("MetaData is null!");
        }

        // 临时文件落盘采用零拷贝+顺序写盘方式非常高效，因此这里采用自旋等待的无锁方式
        for (int i = 0; ; i++) {
            if (!this.recoverableTmpFileHashMap.containsKey(metaDataIndex.hash64())) {
                break;
            }

            if (this.recoverableTmpFileHashMap.size() > RECOVERABLE_TMP_FILE_WARNING_THRESHOLD) {
                // 这种情况经常发生则建议横向扩容（先hardCode，可以考虑做成配置及对接业务监控等）
                log.warn("StorageEngine.recoverableTmpFileHashMap.size() is: " + this.recoverableTmpFileHashMap.size());
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
            throw new ServiceRuntimeException("MetaDataIndex is null!");
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
            throw new ServiceRuntimeException("MetaDataIndex is null!");
        }

        return StorageHandlerFactory.getStorageHandler(metaDataIndex.getStoreEngineVersion()).delete(metaDataIndex);
    }

    /**
     * init
     */
    private void init() {
        try {
            // 存储空间
            this.namespaceMap = new HashMap<>(16);
            FileServerConfigManager.getFileServerConfig().getNamespaces().forEach(item -> {
                String key = item.getName().toLowerCase().trim();
                if (!this.namespaceMap.containsKey(item.getName())) {
                    this.namespaceMap.put(key, item);
                }
            });

            // 目录初始化
            this.baseDir = new File(FileServerConfigManager.getFileServerConfig().getStorageRootPath(),
                    FileServerConfigManager.getCurrentClusterNodeName());
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
            log.error("StorageEngine init error!", e);
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
     * loadRecoverableTmpFile
     */
    private void loadRecoverableTmpFile() {
        /**
         * TODO:
         * 1.加载RecoverableTmpFile集合
         * 2.RecoverableTmpFile集合排序：按照offset排序（必须保障顺序）
         */
    }

    /**
     * cleanupExpiredFiles
     */
    private void cleanupExpiredFiles() {
        /**
         * TODO：
         * 过期文件自动清理
         */
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
}
