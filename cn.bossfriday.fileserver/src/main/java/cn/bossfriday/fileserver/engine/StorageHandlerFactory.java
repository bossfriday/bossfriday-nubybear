package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StorageHandlerFactory {
    private static ConcurrentHashMap<Integer, ITmpFileHandler> tmpFileHandlerImplMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, IStorageHandler> storageHandlerImplMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, IMetaDataHandler> metaDataHandlerImplMap = new ConcurrentHashMap<>();

    static {
        loadStorageHandlerImpl(tmpFileHandlerImplMap, ITmpFileHandler.class);
        loadStorageHandlerImpl(storageHandlerImplMap, IStorageHandler.class);
        loadStorageHandlerImpl(metaDataHandlerImplMap, IMetaDataHandler.class);
    }

    /**
     * init
     */
    public static void init() {
        // just do nothing...
    }

    /**
     * getTmpFileHandler
     */
    public static ITmpFileHandler getTmpFileHandler(int storageEngineVersion) throws Exception {
        if (!tmpFileHandlerImplMap.containsKey(storageEngineVersion)) {
            throw new BizException(ITmpFileHandler.class.getSimpleName() + " implement not existed: " + storageEngineVersion);
        }

        return tmpFileHandlerImplMap.get(storageEngineVersion);
    }

    /**
     * getStorageHandler
     */
    public static IStorageHandler getStorageHandler(int storageEngineVersion) throws Exception {
        if (!storageHandlerImplMap.containsKey(storageEngineVersion)) {
            throw new BizException(IStorageHandler.class.getSimpleName() + " implement not existed: " + storageEngineVersion);
        }

        return storageHandlerImplMap.get(storageEngineVersion);
    }

    /**
     * getMetaDataHandler
     */
    public static IMetaDataHandler getMetaDataHandler(int storageEngineVersion) throws Exception {
        if (!metaDataHandlerImplMap.containsKey(storageEngineVersion)) {
            throw new BizException(IMetaDataHandler.class.getSimpleName() + " implement not existed: " + storageEngineVersion);
        }

        return metaDataHandlerImplMap.get(storageEngineVersion);
    }

    /**
     * 反射加载存储引擎处理实现
     */
    private static void loadStorageHandlerImpl(ConcurrentHashMap map, Class cls) {
        try {
            Set<Class<?>> classes = new Reflections().getSubTypesOf(cls);
            for (Class<?> item : classes) {
                CurrentStorageEngineVersion engineVersion = item.getAnnotation(CurrentStorageEngineVersion.class);
                if (engineVersion == null) {
                    return;
                }

                int version = engineVersion.version();
                if (!map.containsKey(version)) {
                    map.put(version, item.newInstance());
                    log.info("load storage handler done, class:" + item.getName() + ", version:" + version);
                } else {
                    log.warn("duplicated handler, class:" + item.getName() + ", version:" + version);
                }
            }
        } catch (Exception e) {
            log.error("StorageHandlerFactory.load() error!", e);
        }
    }
}

