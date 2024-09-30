package cn.bossfriday.common;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.plugin.IPlugin;
import cn.bossfriday.common.register.ActorRegister;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AbstractServiceBootstrap
 *
 * @author chenx
 */
public abstract class AbstractServiceBootstrap implements IPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceBootstrap.class);

    /**
     * start
     */
    protected abstract void start();

    /**
     * stop
     */
    protected abstract void stop();

    @Override
    public void startup(SystemConfig<?> config) {
        try {
            long begin = System.currentTimeMillis();
            if (config == null) {
                throw new ServiceRuntimeException("ServiceConfig is null");
            }

            // 服务启动
            ClusterRouterFactory.build(config);
            this.registerActor();
            ClusterRouterFactory.getClusterRouter().registryService();
            ClusterRouterFactory.getClusterRouter().startActorSystem();
            this.start();

            // 启动日志
            LOGGER.info("[SystemConfig] {}", config);
            long time = System.currentTimeMillis() - begin;
            String logInfo = "[" + config.getClusterNode().getName() + "] Start Done, RpcPort: " + config.getClusterNode().getPort() + ", Time: " + time;
            CommonUtils.printSeparatedLog(LOGGER, logInfo);
        } catch (InterruptedException interEx) {
            LOGGER.error("Bootstrap.startup() InterruptedException!", interEx);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            LOGGER.error("Bootstrap.startup() error!", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.stop();
        } catch (Exception e) {
            LOGGER.error("service shutdown error!", e);
        }
    }

    /**
     * registerActor
     */
    private void registerActor() {
        List<Class<? extends BaseUntypedActor>> actorClassList = new ArrayList<>();
        Set<Class<? extends BaseUntypedActor>> set = new Reflections().getSubTypesOf(BaseUntypedActor.class);
        actorClassList.addAll(set);

        if (CollectionUtils.isEmpty(actorClassList)) {
            LOGGER.warn("no actor need to register!");
            return;
        }

        actorClassList.forEach(cls -> {
            if (cls.isAnnotationPresent(ActorRoute.class)) {
                ActorRoute route = cls.getAnnotation(ActorRoute.class);
                this.registerActorRoute(cls, route);
            }
        });
    }

    /**
     * registerActorRoute
     *
     * @param cls
     * @param route
     */
    private void registerActorRoute(Class<? extends BaseUntypedActor> cls, ActorRoute route) {
        if (ArrayUtils.isEmpty(route.methods())) {
            return;
        }

        boolean isRegisterByPool = !"".equals(route.poolName()) && route.poolSize() > 0;
        for (String method : route.methods()) {
            try {
                if (isRegisterByPool) {
                    ActorRegister.registerActor(method, cls, getActorExecutorMin(route), getActorExecutorMax(route), route.poolName(), route.poolSize());
                } else {
                    ActorRegister.registerActor(method, cls, getActorExecutorMin(route), getActorExecutorMax(route));
                }

                LOGGER.info("registerActor done: {}", cls.getSimpleName());
            } catch (Exception ex) {
                LOGGER.error("registerActor error!", ex);
            }
        }
    }

    private static final int DEFAULT_MIN;
    private static final int DEFAULT_MAX;

    static {
        DEFAULT_MIN = (Const.CPU_PROCESSORS / 2) <= 0 ? 1 : (Const.CPU_PROCESSORS / 2);
        DEFAULT_MAX = Const.CPU_PROCESSORS;
    }

    /**
     * getActorExecutorMin
     *
     * @param route
     * @return
     */
    private static int getActorExecutorMin(ActorRoute route) {
        if (route.min() > DEFAULT_MIN) {
            return route.min();
        }

        return DEFAULT_MIN;
    }

    /**
     * getActorExecutorMax
     *
     * @param route
     * @return
     */
    private static int getActorExecutorMax(ActorRoute route) {
        if (route.max() > DEFAULT_MAX) {
            return route.max();
        }

        return DEFAULT_MAX;
    }
}
