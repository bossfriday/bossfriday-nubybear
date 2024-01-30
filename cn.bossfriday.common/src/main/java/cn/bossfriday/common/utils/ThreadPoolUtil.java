package cn.bossfriday.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.*;

/**
 * ThreadPoolUtil
 *
 * @author chenx
 */
public class ThreadPoolUtil {

    private static final ConcurrentHashMap<String, ExecutorService> THREAD_MAP = new ConcurrentHashMap<>();
    public static final int AVAILABLE_PROCESSORS;
    private static final String THREAD_COMMON = "common";
    private static final String THREAD_NAME_PLACEHOLDER = "%d";

    static {
        AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    }

    private ThreadPoolUtil() {

    }

    /**
     * getCommonThreadPool
     */
    public static ExecutorService getCommonThreadPool() {
        return getThreadPool(THREAD_COMMON, Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * getSingleThreadExecutor
     *
     * @param name
     * @return
     */
    public static ExecutorService getSingleThreadExecutor(String name) {
        return getThreadPool(name, name, 1, 1, 0, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * ExecutorService
     *
     * @param name
     * @param workerQueueSize
     * @return
     */
    public static ExecutorService getSingleThreadExecutor(String name, int workerQueueSize) {
        return getThreadPool(name, name, 1, 1, workerQueueSize, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * getThreadPool
     *
     * @param name
     * @return
     */
    public static ExecutorService getThreadPool(String name) {
        return getThreadPool(name, ThreadPoolUtil.AVAILABLE_PROCESSORS);
    }

    /**
     * getThreadPool
     *
     * @param name
     * @param size
     * @return
     */
    public static ExecutorService getThreadPool(String name, int size) {
        return getThreadPool(name, name, size);
    }

    /**
     * getThreadPool
     *
     * @param name
     * @param threadNamePrefix
     * @param coreSize
     * @return
     */
    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize) {
        return getThreadPool(name, threadNamePrefix, coreSize, 0);
    }

    /**
     * getThreadPool
     *
     * @param name
     * @param threadNamePrefix
     * @param coreSize
     * @param workerQueueSize
     * @return
     */
    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize,
                                                int workerQueueSize) {
        return getThreadPool(name, threadNamePrefix, coreSize, coreSize * 2, workerQueueSize, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * getThreadPool
     *
     * @param name             线程池名称,
     * @param threadNamePrefix 线程名称前缀.
     * @param coreSize         线程数量. 必须> 1
     * @param maxThreadSize    最大数量. 必须> 1 ,并且大于 coreSize ,否则使用coreSize
     * @param workerQueueSize  线程队列数量, 当 workerQueueSize <=0  workerQueueSize:使用默认值 Integer.MAX_VALUE
     * @param rejectedHandler  拒绝策略, 如果为空 使用 ThreadPoolExecutor.AbortPolicy()
     */
    public static ExecutorService getThreadPool(String name,
                                                String threadNamePrefix,
                                                int coreSize,
                                                int maxThreadSize,
                                                int workerQueueSize,
                                                RejectedExecutionHandler rejectedHandler) {
        if (!THREAD_MAP.containsKey(name)) {
            ExecutorService pool = new ThreadPoolExecutor(
                    getCoreSize(name, coreSize),
                    maxThreadSize > coreSize ? maxThreadSize : coreSize, 0L,
                    TimeUnit.MILLISECONDS,
                    getWorkerBlockingQueue(workerQueueSize),
                    getThreadFactory(name, threadNamePrefix),
                    rejectedHandler == null ? new ThreadPoolExecutor.AbortPolicy() : rejectedHandler);
            ExecutorService existedPool = THREAD_MAP.putIfAbsent(name, pool);
            if (existedPool != null) {
                pool.shutdown();
            }
        }

        return THREAD_MAP.get(name);
    }

    /**
     * getThreadFactory
     *
     * @param name
     * @param threadNamePrefix
     * @return
     */
    public static ThreadFactory getThreadFactory(String name, String threadNamePrefix) {
        if (StringUtils.isBlank(threadNamePrefix)) {
            threadNamePrefix = name;
        }

        if (!threadNamePrefix.contains(THREAD_NAME_PLACEHOLDER)) {
            threadNamePrefix += "_" + THREAD_NAME_PLACEHOLDER;
        }

        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix).build();
    }

    /**
     * BlockingQueue
     *
     * @param workerQueueSize
     * @return
     */
    private static BlockingQueue<Runnable> getWorkerBlockingQueue(int workerQueueSize) {
        int queueMaxSize = workerQueueSize > 0 ? workerQueueSize : Integer.MAX_VALUE;

        return new LinkedBlockingQueue<>(queueMaxSize);
    }

    /**
     * getCoreSize：未将来有配置优先走配置留统一处理口子
     *
     * @param name
     * @param coreSize
     * @return
     */
    @SuppressWarnings("squid:S1172")
    private static int getCoreSize(String name, int coreSize) {
        // 有配置优先走配置
        return coreSize;
    }
}
