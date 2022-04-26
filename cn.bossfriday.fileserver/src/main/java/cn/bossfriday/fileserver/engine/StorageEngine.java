package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.common.conf.StorageNamespace;
import cn.bossfriday.fileserver.engine.core.BaseStorageEngine;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.entity.ChunkedFileData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.engine.entity.RecoverableTmpFile;
import cn.bossfriday.fileserver.engine.entity.StorageIndex;
import cn.bossfriday.fileserver.engine.enums.StorageEngineVersion;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.bossfriday.common.Const.EACH_RECEIVE_QUEUE_SIZE;
import static cn.bossfriday.fileserver.common.FileServerConst.FILE_PATH_TMP;

@Slf4j
public class StorageEngine extends BaseStorageEngine {
    private volatile static StorageEngine instance = null;
    private ConcurrentHashMap<String, StorageIndex> storageIndexMap;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Getter
    private File baseDir;   // 存储根目录

    @Getter
    private File tmpDir;    // 存储临时目录

    @Getter
    private HashMap<String, StorageNamespace> namespaceMap;    // 存储空间

    private StorageEngine() {
        super(EACH_RECEIVE_QUEUE_SIZE);
        init();
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

    /**
     * start
     */
    public void start() throws Exception {
        // todo:临时文件落盘恢复、过期文件清理任务启动（包含过期临时文件清理）、临时文件落盘……
        super.start();
        loadStorageIndex();
        log.info("StorageEngine start done: " + FileServerConfigManager.getCurrentClusterNodeName());
    }

    /**
     * stop
     */
    public void stop() {
        super.queue.shutdown();
    }

    @Override
    protected void onRecoverableTmpFileEvent(RecoverableTmpFile event) {
        String fileTransactionId = "";
        try {
            IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(event.getStoreEngineVersion());
            storageHandler.apply(event);
        } catch (Exception ex) {
            log.error("onRecoverableTmpFileEvent() error!" + fileTransactionId, ex);
        }
    }

    /**
     * 文件上传
     */
    public MetaDataIndex upload(WriteTmpFileResult data) {
        String fileTransactionId = "";
        try {
            if (data == null)
                throw new BizException("WriteTmpFileResult is null!");

            fileTransactionId = data.getFileTransactionId();
            int engineVersion = data.getStorageEngineVersion();
            IStorageHandler storageHandler = StorageHandlerFactory.getStorageHandler(engineVersion);
            IMetaDataHandler metaDataHandler = StorageHandlerFactory.getMetaDataHandler(engineVersion);
            ITmpFileHandler tmpFileHandler = StorageHandlerFactory.getTmpFileHandler(engineVersion);
            long metaDataLength = metaDataHandler.getLength(data.getFileName(), data.getFileTotalSize());

            StorageIndex resultIndex = null;
            StorageIndex currentStorageIndex = getStorageIndex(data.getNamespace(), engineVersion);
            lock.writeLock().lock();
            try {
                resultIndex = storageHandler.ask(currentStorageIndex, metaDataLength);
            } finally {
                lock.writeLock().unlock();
            }

            if (resultIndex == null)
                throw new BizException("Result StorageIndex is null: " + data.getFileTransactionId());

            long metaDataIndexOffset = resultIndex.getOffset() - metaDataLength;
            if (metaDataIndexOffset < 0)
                throw new BizException("metaDataIndexOffset <0: " + data.getFileTransactionId());

            MetaDataIndex metaDataIndex = MetaDataIndex.builder()
                    .clusterNode(data.getClusterNodeName())
                    .storeEngineVersion(engineVersion)
                    .namespace(data.getNamespace())
                    .timestamp(resultIndex.getTime())
                    .offset(metaDataIndexOffset)
                    .fileExtName(data.getFileExtName())
                    .build();

            String recoverableTmpFileName = storageHandler.getRecoverableTmpFileName(metaDataIndex, data.getFileExtName());
            String recoverableTmpFilePath = tmpFileHandler.rename(data.getFilePath(), recoverableTmpFileName);
            RecoverableTmpFile recoverableTmpFile = RecoverableTmpFile.builder()
                    .fileTransactionId(fileTransactionId)
                    .storeEngineVersion(data.getStorageEngineVersion())
                    .namespace(data.getNamespace())
                    .time(resultIndex.getTime())
                    .offset(metaDataIndex.getOffset())
                    .timestamp(data.getTimestamp())
                    .fileName(data.getFileName())
                    .fileTotalSize(data.getFileTotalSize())
                    .filePath(recoverableTmpFilePath)
                    .build();
            this.publishEvent(recoverableTmpFile);

            return metaDataIndex;
        } catch (Exception ex) {
            log.error("upload error: " + fileTransactionId, ex);
        }

        return null;
    }

    /**
     * 分片下载
     */
    public ChunkedFileData chunkedDownload(String fileTransactionId, MetaDataIndex metaDataIndex, long chunkIndex) {
        try {
            if (metaDataIndex == null)
                throw new BizException("MetaDataIndex is null!");

            log.info("----->" + metaDataIndex.toString());

        } catch (Exception ex) {
            log.error("chunkedDownload error: " + fileTransactionId, ex);
        }

        return null;
    }

    private void init() {
        try {
            // 存储空间
            namespaceMap = new HashMap<>();
            FileServerConfigManager.getFileServerConfig().getNamespaces().forEach(item -> {
                String key = item.getName().toLowerCase().trim();
                if (!namespaceMap.containsKey(item.getName())) {
                    namespaceMap.put(key, item);
                }
            });


            // 目录初始化
            baseDir = new File(FileServerConfigManager.getFileServerConfig().getStorageRootPath(),
                    FileServerConfigManager.getCurrentClusterNodeName());
            if (!baseDir.exists())
                baseDir.mkdirs();

            for (String spaceName : namespaceMap.keySet()) {
                File storageNamespaceDir = new File(baseDir, spaceName);
                if (!storageNamespaceDir.exists())
                    storageNamespaceDir.mkdirs();
            }

            tmpDir = new File(baseDir, FILE_PATH_TMP);
            if (!tmpDir.exists())
                tmpDir.mkdirs();
        } catch (Exception e) {
            log.error("StorageEngine init error!", e);
        }
    }

    private void loadStorageIndex() throws Exception {
        storageIndexMap = new ConcurrentHashMap<>();
        StorageEngineVersion[] versions = StorageEngineVersion.class.getEnumConstants();
        for (String namespace : namespaceMap.keySet()) {
            for (StorageEngineVersion item : versions) {
                int version = item.getValue();
                IStorageHandler handler = StorageHandlerFactory.getStorageHandler(version);
                StorageIndex storageIndex = handler.getStorageIndex(namespace);
                storageIndexMap.put(getStorageIndexMapKey(namespace, version), storageIndex);
                log.info(storageIndex.toString());
            }
        }
    }

    private static String getStorageIndexMapKey(String namespace, int storageEngineVersion) {
        return namespace + "-" + storageEngineVersion;
    }

    private StorageIndex getStorageIndex(String namespace, int storageEngineVersion) throws Exception {
        String key = getStorageIndexMapKey(namespace, storageEngineVersion);
        if (!storageIndexMap.containsKey(key))
            throw new BizException("StorageIndex not existed: " + key);

        return storageIndexMap.get(key);
    }
}
