package cn.bossfriday.common.register;

import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.utils.ThreadPoolUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * ActorRegister
 *
 * @author chenx
 */
public class ActorRegister {

    private static ConcurrentHashMap<String, ExecutorService> poolMap = new ConcurrentHashMap<>();

    private ActorRegister() {

    }

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     * @param pool
     */
    public static void registerActor(String method, Class<? extends BaseUntypedActor> cls, int min, int max, ExecutorService pool) {
        ClusterRouterFactory.getClusterRouter().registerActor(method, cls, min, max, pool);
    }

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     */
    public static void registerActor(String method, Class<? extends BaseUntypedActor> cls, int min, int max) {
        ClusterRouterFactory.getClusterRouter().registerActor(method, cls, min, max);
    }

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     * @param poolName
     * @param poolThreadSize
     * @throws Exception
     */
    public static void registerActor(String method, Class<? extends BaseUntypedActor> cls, int min, int max, String poolName, int poolThreadSize) {
        if (!poolMap.containsKey(poolName)) {
            ExecutorService pool = ThreadPoolUtil.getThreadPool(poolName, poolThreadSize);
            poolMap.putIfAbsent(poolName, pool);
        }

        registerActor(method, cls, min, max, poolMap.get(poolName));
    }
}
