package cn.bossfriday.fileserver.engine;

import cn.bossfriday.common.utils.ThreadPoolUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class StorageDispatcher {
    private static int uploadThreadSize = 128;
    private static ExecutorService[] uploadThreads = null;

    static {
        uploadThreads = new ExecutorService[uploadThreadSize];
        for (int i = 0; i < uploadThreadSize; i++) {
            uploadThreads[i] = getSingleThreadExecutor("uploadThread-" + i);
        }
    }

    /**
     * getUploadThread
     *
     * @param resourceId
     * @return
     */
    public static ExecutorService getUploadThread(String resourceId) {
        int id = Math.abs(resourceId.hashCode()) % uploadThreadSize;
        return uploadThreads[id];
    }

    private static ExecutorService getSingleThreadExecutor(String name) {
        ThreadFactory factory = ThreadPoolUtil.getThreadFactory(name, name);
        return Executors.newSingleThreadExecutor(factory);
    }
}
