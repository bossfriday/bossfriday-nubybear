package cn.bossfriday.common.router;

import cn.bossfriday.common.conf.ServiceConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * ClusterRouterFactory
 *
 * @author chenx
 */
@Slf4j
public class ClusterRouterFactory {

    private static volatile ClusterRouter clusterRouter;

    private ClusterRouterFactory() {

    }

    /**
     * build
     *
     * @param serviceConfig
     * @throws InterruptedException
     */
    public static void build(ServiceConfig serviceConfig) throws InterruptedException {
        if (clusterRouter == null) {
            synchronized (ClusterRouterFactory.class) {
                if (clusterRouter == null) {
                    clusterRouter = new ClusterRouter(serviceConfig.getSystemName(),
                            serviceConfig.getZkAddress(),
                            serviceConfig.getClusterNode().getName(),
                            serviceConfig.getClusterNode().getHost(),
                            serviceConfig.getClusterNode().getPort(),
                            serviceConfig.getClusterNode().getVirtualNodesNum());
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
