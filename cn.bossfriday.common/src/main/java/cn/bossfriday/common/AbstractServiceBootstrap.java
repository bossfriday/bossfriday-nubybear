package cn.bossfriday.common;

import cn.bossfriday.common.conf.ServiceConfig;
import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.plugin.IPlugin;
import cn.bossfriday.common.plugin.PluginElement;
import cn.bossfriday.common.register.ActorRegister;
import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.utils.ClassLoaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AbstractServiceBootstrap
 *
 * @author chenx
 */
@Slf4j
public abstract class AbstractServiceBootstrap implements IPlugin {

    /**
     * start
     */
    protected abstract void start();

    /**
     * stop
     */
    protected abstract void stop();

    @Override
    public void startup(ServiceConfig config) {
        try {
            if (config == null) {
                throw new BizException("ServiceConfig is null");
            }

            ClusterRouterFactory.build(config);
            this.registerActor(config);
            ClusterRouterFactory.getClusterRouter().registryService();
            ClusterRouterFactory.getClusterRouter().startActorSystem();
            this.start();
            log.info(config.getSystemName() + " startup() done.");
        } catch (Exception ex) {
            log.error("Bootstrap.startup() error!", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.stop();
        } catch (Exception e) {
            log.error("service shutdown error!", e);
        }
    }

    /**
     * registerActor
     *
     * @param config
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void registerActor(ServiceConfig config) throws IOException, ClassNotFoundException {
        List<Class<? extends BaseUntypedActor>> classList = new ArrayList<>();
        this.loadActor(classList, config);
        if (CollectionUtils.isEmpty(classList)) {
            log.warn("no actor need to register!");
            return;
        }

        classList.forEach(cls -> {
            if (cls.isAnnotationPresent(ActorRoute.class)) {
                ActorRoute route = cls.getAnnotation(ActorRoute.class);
                this.registerActorRoute(cls, route);
            }
        });
    }

    /**
     * loadActor(有配置走配置，无配置反射获取当前jar包内所有UntypedActor类)
     *
     * @param classList
     * @param config
     */
    private void loadActor(List<Class<? extends BaseUntypedActor>> classList, ServiceConfig config) throws IOException, ClassNotFoundException {
        List<PluginElement> pluginElements = config.getPluginElements();
        if (!CollectionUtils.isEmpty(pluginElements)) {
            for (PluginElement pluginConfig : pluginElements) {
                File file = new File(pluginConfig.getPath());
                if (!file.exists()) {
                    log.warn("service build not existed!(" + pluginConfig.getPath() + ")");
                    continue;
                }

                List<Class<? extends BaseUntypedActor>> list = ClassLoaderUtil.getAllClass(pluginConfig.getPath(), BaseUntypedActor.class);
                classList.addAll(list);
            }
        } else {
            Set<Class<? extends BaseUntypedActor>> set = new Reflections().getSubTypesOf(BaseUntypedActor.class);
            classList.addAll(set);
        }
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

                log.info("registerActor done: " + cls.getSimpleName());
            } catch (Exception ex) {
                log.error("registerActor error!", ex);
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
