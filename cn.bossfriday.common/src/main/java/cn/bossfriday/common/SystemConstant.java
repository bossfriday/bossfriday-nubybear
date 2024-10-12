package cn.bossfriday.common;

/**
 * SystemConstant
 *
 * @author chenx
 */
public class SystemConstant {

    private static final int CPUS;

    static {
        CPUS = Runtime.getRuntime().availableProcessors();
    }

    private SystemConstant() {
        // do nothing
    }

    /**
     * common
     */
    public static final int CPU_PROCESSORS = CPUS;
    public static final String PATH_DELIMITER = "/";

    /**
     * thread pool name
     */
    public static final String THREAD_POOL_NAME_ACTORS_DISPATCH = "Actors_Dispatch";
    public static final String THREAD_POOL_NAME_ACTORS_POOLS = "Actors_Pools";
    public static final String THREAD_POOL_NAME_ACTORS_CALLBACK = "Actors_CallBack";
    public static final String ZK_CLIENT_THREAD_POOL = "Actors_CallBack";

    /**
     * actor
     */
    public static final String DEAD_LETTER_ACTOR_HOST = "0.0.0.0";
    public static final int DEAD_LETTER_ACTOR_PORT = 0;
    public static final long DEFAULT_CALLBACK_ACTOR_TTL = 5000L;

    /**
     * queues（最好做成配置）
     */
    public static final int EACH_RECEIVE_QUEUE_SIZE = 1024 * 1024;
    public static final int EACH_SEND_QUEUE_SIZE = 1024 * 1024;
    public static final int SLOW_QUEUE_THRESHOLD = 500;

    /**
     * zk
     */
    public static final String ZK_PATH_CLUSTER_NODE = "clusterNodes";
}
