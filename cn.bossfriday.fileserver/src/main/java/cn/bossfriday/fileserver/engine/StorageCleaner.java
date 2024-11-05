package cn.bossfriday.fileserver.engine;

import cn.bossfriday.im.common.entity.conf.FileServerConfig;
import cn.bossfriday.im.common.entity.conf.StorageNamespace;
import cn.bossfriday.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * StorageCleaner
 *
 * @author chenx
 */
@Slf4j
public class StorageCleaner {

    private static final int FIRST_DELAY = 60;
    private static final TimeUnit PERIOD_TIME_UNIT = TimeUnit.SECONDS;

    private ScheduledExecutorService scheduler;
    private final FileServerConfig config;

    public StorageCleaner(FileServerConfig config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * startup
     */
    public void startup() {
        try {
            this.scheduler.scheduleAtFixedRate(new CleaningTask(this.config),
                    FIRST_DELAY,
                    this.config.getCleanerScanInterval(),
                    PERIOD_TIME_UNIT);
        } catch (Exception ex) {
            log.error("StorageCleaner.start() error!", ex);
        }
    }

    /**
     * shutdown
     */
    public void shutdown() {
        try {
            this.scheduler.shutdown();
        } catch (Exception ex) {
            log.error("StorageCleaner.stop() error!", ex);
        }
    }

    /**
     * CleaningTask
     */
    private static class CleaningTask implements Runnable {

        private final FileServerConfig config;

        private static final String REG_DAY_DIR_NAME = "\\d{4}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])";

        public CleaningTask(FileServerConfig config) {
            this.config = config;
        }

        @Override
        public void run() {
            try {
                log.info("==========CleaningTask Begin==========");
                for (StorageNamespace namespace : this.config.getNamespaces()) {
                    cleanExpiredFiles(namespace);
                }
                log.info("===========CleaningTask End===========");
            } catch (Exception ex) {
                log.error("StorageCleaner.CleaningTask.run() error!", ex);
            }
        }

        /**
         * cleanExpiredFiles
         *
         * @param storageNamespace
         */
        private static void cleanExpiredFiles(StorageNamespace storageNamespace) {
            try {
                if (storageNamespace.getExpireDay() <= 0) {
                    return;
                }

                File storageNamespaceDir = new File(StorageEngine.getInstance().getBaseDir(), storageNamespace.getName());
                if (!storageNamespaceDir.exists()) {
                    return;
                }

                File[] dayDirList = storageNamespaceDir.listFiles();
                for (File dayDir : dayDirList) {
                    dayDirProcess(dayDir, storageNamespace.getExpireDay());
                }

                log.info("CleaningTask.cleanExpiredFiles() done, namespace: {}", storageNamespace.getName());
            } catch (Exception ex) {
                log.error("StorageCleaner.cleanExpiredFiles() error!", ex);
            }
        }

        /**
         * dayDirProcess
         *
         * @param dayDir
         * @param expiredDay
         */
        private static void dayDirProcess(File dayDir, int expiredDay) {
            try {
                if (!dayDir.isDirectory() || !Pattern.matches(REG_DAY_DIR_NAME, dayDir.getName())) {
                    return;
                }

                if (!isExpired(dayDir.getName(), expiredDay)) {
                    return;
                }

                FileUtils.deleteDirectory(dayDir);
                log.info("delete directory done: {}", dayDir.getAbsolutePath());
            } catch (Exception ex) {
                log.error("StorageCleaner.dayDirProcess() error!", ex);
            }
        }

        /**
         * isExpired
         *
         * @param dayDirName
         * @param expiredDay
         * @return
         * @throws ParseException
         */
        private static boolean isExpired(String dayDirName, int expiredDay) throws ParseException {
            Date date = DateUtil.str2Date(dayDirName, DateUtil.DEFAULT_DATE_HYPHEN_FORMAT);
            if (date == null) {
                return false;
            }

            long day = (new Date().getTime() - date.getTime()) / (24 * 60 * 60 * 1000);

            return day >= expiredDay;
        }
    }
}
