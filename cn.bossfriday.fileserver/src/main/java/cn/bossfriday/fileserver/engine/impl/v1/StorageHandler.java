package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.DateUtil;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.IStorageHandler;
import cn.bossfriday.fileserver.engine.entity.StorageIndex;
import cn.bossfriday.fileserver.engine.enums.StorageEngineVersion;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

import static cn.bossfriday.fileserver.common.FileServerConst.STORAGE_FILE_EXTENSION_NAME;

@Slf4j
@CurrentStorageEngineVersion
public class StorageHandler implements IStorageHandler {

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
    public StorageIndex askStorage(StorageIndex storageIndex, long dataLength) throws Exception {
        if (storageIndex == null)
            throw new BizException("storageIndex is null");

        if (dataLength <= 0)
            throw new BizException("dataLength <= 0");

        int currentTime = Integer.parseInt(DateUtil.date2Str(new Date(), DateUtil.DEFAULT_DATE_HYPHEN_FORMAT));
        if (storageIndex.getTime() != currentTime) {
            // 跨天重新初始化StorageIndex
            storageIndex = getStorageIndex(storageIndex.getNamespace());
        }

        storageIndex.addOffset(dataLength);

        return storageIndex.clone();
    }

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
}
