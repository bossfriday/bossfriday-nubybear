package cn.bossfriday.common.router;

import cn.bossfriday.common.common.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * ClusterRouterFactory
 *
 * @author chenx
 */
@Slf4j
public class ClusterRouterFactory {

    @SuppressWarnings("squid:S3077")
    private static volatile ClusterRouter clusterRouter;

    private ClusterRouterFactory() {

    }

    /**
     * build
     *
     * @param systemConfig
     * @throws InterruptedException
     */
    public static void build(SystemConfig systemConfig) throws Exception {
        if (clusterRouter == null) {
            synchronized (ClusterRouterFactory.class) {
                if (clusterRouter == null) {
                    clusterRouter = new ClusterRouter(systemConfig.getSystemName(),
                            systemConfig.getZkAddress(),
                            systemConfig.getClusterNode().getName(),
                            systemConfig.getClusterNode().getHost(),
                            systemConfig.getClusterNode().getPort(),
                            systemConfig.getClusterNode().getVirtualNodesNum());
                }
            }
        }
    }

    /**
     * getClusterRouter
     *
     * @return
     */
    public static ClusterRouter getClusterRouter() {
        if (clusterRouter == null) {
            throw new ServiceRuntimeException("plz invoke build() firstly!");
        }

        return clusterRouter;
    }
}
