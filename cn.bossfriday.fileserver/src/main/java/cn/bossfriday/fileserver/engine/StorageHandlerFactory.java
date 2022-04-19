package cn.bossfriday.fileserver.engine;

import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.core.StorageEngineVersion;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StorageHandlerFactory {
    private static ConcurrentHashMap<Integer, ITmpFileHandler> tmpFileHandlerImplMap = new ConcurrentHashMap<>();

    static {
        loadStorageHandlerImpl(tmpFileHandlerImplMap, ITmpFileHandler.class);
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
        if (tmpFileHandlerImplMap.containsKey(storageEngineVersion)) {
            return tmpFileHandlerImplMap.get(storageEngineVersion);
        }

        return null;
    }

    /**
     * 反射加载存储引擎处理实现
     */
    private static void loadStorageHandlerImpl(ConcurrentHashMap map, Class cls) {
        try {
            Set<Class<?>> classes = new Reflections().getSubTypesOf(cls);
            for (Class<?> item : classes) {
                StorageEngineVersion engineVersion = item.getAnnotation(StorageEngineVersion.class);
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

