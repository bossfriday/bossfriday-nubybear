package cn.bossfriday.fileserver.engine;

import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static cn.bossfriday.fileserver.common.FileServerConst.FILE_PATH_TMP;

@Slf4j
public class StorageEngine {
    @Getter
    private File baseDir;   // 存储根目录

    @Getter
    private File tmpDir;    // 存储临时目录

    private volatile static StorageEngine instance = null;

    private StorageEngine() {
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
    public void start() {
        // todo:临时文件落盘恢复、过期文件清理任务启动（包含过期临时文件清理）、临时文件落盘……
        log.info("StorageEngine start done: " + FileServerConfigManager.getCurrentClusterNodeName());
    }

    /**
     * 文件上传
     */
    public MetaDataIndex upload(WriteTmpFileResult data) {
        return null;
    }

    private void init() {
        try {
            // 目录初始化
            baseDir = new File(FileServerConfigManager.getFileServerConfig().getStorageRootPath(),
                    FileServerConfigManager.getCurrentClusterNodeName());
            if (!baseDir.exists())
                baseDir.mkdirs();

            tmpDir = new File(baseDir, FILE_PATH_TMP);
            if (!tmpDir.exists())
                tmpDir.mkdirs();
        } catch (Exception e) {
            log.error("StorageEngine init error!", e);
        }
    }
}
