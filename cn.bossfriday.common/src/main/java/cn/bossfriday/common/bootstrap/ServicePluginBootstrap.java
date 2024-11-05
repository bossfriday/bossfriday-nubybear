package cn.bossfriday.common.bootstrap;

import cn.bossfriday.common.common.SystemConstant;
import cn.bossfriday.common.common.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.plugin.IPlugin;
import cn.bossfriday.common.register.ActorRegister;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.utils.ClassLoaderUtil;
import cn.bossfriday.common.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ServicePluginBootstrap
 *
 * @author chenx
 */
public abstract class ServicePluginBootstrap implements IPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePluginBootstrap.class);

    /**
     * 服务包名（服务启动只获取该包下所有的 actor 进行注册）
     */
    protected abstract String getServiceName();

    /**
     * start
     */
    protected abstract void start();

    /**
     * stop
     */
    protected abstract void stop();

    @Override
    public void startup(SystemConfig config) {
        try {
            long begin = System.currentTimeMillis();
            if (config == null) {
                throw new ServiceRuntimeException("ServiceConfig is null");
            }

            // 服务启动
            ClusterRouterFactory.build(config);
            this.registerActor(config);
            ClusterRouterFactory.getClusterRouter().registryService();
            ClusterRouterFactory.getClusterRouter().startActorSystem();
            this.start();

            // 启动日志
            long time = System.currentTimeMillis() - begin;
            String logInfo = "[" + config.getClusterNode().getName() + "]-[" + this.getServiceName() + "] Start Done, RpcPort: " + config.getClusterNode().getPort() + ", Time: " + time;
            CommonUtils.printSeparatedLog(LOGGER, logInfo);
        } catch (InterruptedException interEx) {
            LOGGER.error("ServicePluginBootstrap.startup() InterruptedException!", interEx);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            LOGGER.error("ServicePluginBootstrap.startup() error!", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.stop();
        } catch (Exception ex) {
            LOGGER.error("ServicePluginBootstrap.shutdown() error!", ex);
        }
    }

    /**
     * registerActor
     */
    private void registerActor(SystemConfig config) throws IOException, ClassNotFoundException {
        List<Class<? extends BaseUntypedActor>> actorClassList = new ArrayList<>();

        // 有配置走配置
        if (!CollectionUtils.isEmpty(config.getPluginJarFilePath())) {
            for (String jarFilePath : config.getPluginJarFilePath()) {
                if (StringUtils.isNotEmpty(jarFilePath)) {
                    File file = new File(jarFilePath);
                    if (!file.exists()) {
                        LOGGER.warn("PluginJarFile not existed! path={}", jarFilePath);
                        continue;
                    }

                    List<Class<? extends BaseUntypedActor>> list = ClassLoaderUtil.getAllClass(jarFilePath, BaseUntypedActor.class);
                    actorClassList.addAll(list);
                }
            }
        } else {
            // 无配置反射获取当前jar包
            Set<Class<? extends BaseUntypedActor>> set = new Reflections(this.getClass().getPackage().getName()).getSubTypesOf(BaseUntypedActor.class);
            actorClassList.addAll(set);
        }

        if (CollectionUtils.isEmpty(actorClassList)) {
            LOGGER.warn("no actor need to register!");
            return;
        }

        // registerActor
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
        DEFAULT_MIN = (SystemConstant.CPU_PROCESSORS / 2) <= 0 ? 1 : (SystemConstant.CPU_PROCESSORS / 2);
        DEFAULT_MAX = SystemConstant.CPU_PROCESSORS;
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
