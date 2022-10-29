package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.common.utils.ThreadPoolUtil;

import java.util.concurrent.ExecutorService;

/**
 * StorageDispatcher
 *
 * @author chenx
 */
public class StorageDispatcher {

    private static int uploadThreadSize = 128;
    private static ExecutorService[] uploadThreads = null;

    static {
        uploadThreads = new ExecutorService[uploadThreadSize];
        for (int i = 0; i < uploadThreadSize; i++) {
            uploadThreads[i] = ThreadPoolUtil.getSingleThreadExecutor("uploadThread-" + i);
        }
    }

    private StorageDispatcher() {

    }

    /**
     * getUploadThread
     *
     * @param resourceId
     * @return
     */
    public static ExecutorService getUploadThread(String resourceId) {
        int resourceIdHash = MurmurHashUtil.hash32(resourceId);

        // Math.abs(Integer.MIN_VALUE) 为负数
        if (resourceIdHash == Integer.MIN_VALUE) {
            return uploadThreads[0];
        }

        return uploadThreads[Math.abs(resourceIdHash) % uploadThreadSize];
    }
}
