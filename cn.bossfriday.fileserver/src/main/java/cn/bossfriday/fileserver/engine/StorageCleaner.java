package cn.bossfriday.fileserver.engine;

import cn.bossfriday.fileserver.common.conf.FileServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * StorageCleaner
 *
 * @author chenx
 */
@Slf4j
public class StorageCleaner {

    private static final int PERIOD = 5;
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
            this.scheduler.scheduleAtFixedRate(new CleaningTask(), PERIOD, PERIOD, PERIOD_TIME_UNIT);
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
     * cleanExpiredFiles
     */
    private static void cleanExpiredFiles() {
        try {
           
        } catch (Exception ex) {
            log.error("StorageCleaner.cleanExpiredFiles() error!", ex);
        }
    }

    /**
     * CleaningTask
     */
    private static class CleaningTask implements Runnable {
        @Override
        public void run() {
            cleanExpiredFiles();
        }
    }
}
