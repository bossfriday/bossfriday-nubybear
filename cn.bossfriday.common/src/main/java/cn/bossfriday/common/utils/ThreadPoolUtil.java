package cn.bossfriday.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.*;

public class ThreadPoolUtil {
    private static final ConcurrentHashMap<String, ExecutorService> threadMap = new ConcurrentHashMap<>();
    public static final int AVAILABLE_PROCESSORS;
    private static final String THREAD_COMMON = "common";

    static {
        AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    }

    /**
     * getCommonThreadPool
     */
    public static ExecutorService getCommonThreadPool() {
        return getThreadPool(THREAD_COMMON, Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * getThreadPool
     */
    public static ExecutorService getThreadPool(String name) {
        return getThreadPool(name, ThreadPoolUtil.AVAILABLE_PROCESSORS);
    }

    public static ExecutorService getThreadPool(String name, int size) {
        return getThreadPool(name, name, size);
    }

    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize) {
        return getThreadPool(name, threadNamePrefix, coreSize, 0);
    }

    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize,
                                                int workerQueueSize) {
        return getThreadPool(name, threadNamePrefix, coreSize, coreSize * 2, workerQueueSize, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * @param name 线程池名称,
     * @param threadNamePrefix 线程名称前缀.
     * @param coreSize 线程数量. 必须> 1
     * @param maxThreadSize 最大数量. 必须> 1 ,并且大于 coreSize ,否则使用coreSize
     * @param workerQueueSize 线程队列数量, 当 workerQueueSize <=0  workerQueueSize:使用默认值 Integer.MAX_VALUE
     * @param rejectedHandler 拒绝策略, 如果为空 使用 ThreadPoolExecutor.AbortPolicy()
     */
    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize,
                                                int maxThreadSize,
                                                int workerQueueSize,
                                                RejectedExecutionHandler rejectedHandler) {
        if (!threadMap.containsKey(name)) {
            ExecutorService pool = new ThreadPoolExecutor(
                    getCoreSize(name, coreSize),
                    maxThreadSize > coreSize ? maxThreadSize : coreSize, 0L,
                    TimeUnit.MILLISECONDS,
                    getWorkerBlockingQueue(workerQueueSize),
                    getThreadFactory(name, threadNamePrefix),
                    rejectedHandler == null ? new ThreadPoolExecutor.AbortPolicy() : rejectedHandler);

            ExecutorService existedPool = threadMap.putIfAbsent(name, pool);
            if (existedPool != null) {
                pool.shutdown();
            }
        }

        return threadMap.get(name);
    }

    private static ThreadFactory getThreadFactory(String name, String threadNamePrefix) {
        if (StringUtils.isBlank(threadNamePrefix)) {
            threadNamePrefix = name;
        }

        if (!threadNamePrefix.contains("%d")) {
            threadNamePrefix += "_%d";
        }

        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix).build();
    }

    private static BlockingQueue getWorkerBlockingQueue(int workerQueueSize) {
        int queueMaxSize = workerQueueSize > 0 ? workerQueueSize : Integer.MAX_VALUE;

        return new LinkedBlockingQueue(queueMaxSize);
    }

    /**
     * getCoreSize：未将来有配置优先走配置留统一处理口子
     * @param name
     * @param coreSize
     * @return
     */
    private static int getCoreSize(String name, int coreSize) {
        // 有配置优先走配置
        return coreSize;
    }
}
