package cn.bossfriday.common.bootstrap;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.http.HttpProcessorMapper;
import cn.bossfriday.common.http.IHttpProcessor;
import cn.bossfriday.common.plugin.IPlugin;
import cn.bossfriday.common.plugin.PluginType;
import cn.bossfriday.common.register.HttpApiRoute;
import cn.bossfriday.common.utils.ClassLoaderUtil;
import cn.bossfriday.common.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * HttpApiPluginBootstrap
 *
 * @author chenx
 */
public abstract class HttpApiPluginBootstrap implements IPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpApiPluginBootstrap.class);

    /**
     * 服务包名（服务启动只获取该包下所有的 IHttpProcessor 进行装载）
     */
    protected abstract PluginType getPluginType();

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
            this.loadHttpProcessor(config);
            this.start();

            // 启动日志
            long time = System.currentTimeMillis() - begin;
            String logInfo = "[" + config.getClusterNode().getName() + "]-[" + this.getPluginType().getServiceName() + "] Start Done, Time: " + time;
            CommonUtils.printSeparatedLog(LOGGER, logInfo);
        } catch (Exception ex) {
            LOGGER.error("HttpApiPluginBootstrap.error!", ex);
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
     * loadHttpProcessor
     */
    @SuppressWarnings("squid:S3776")
    private void loadHttpProcessor(SystemConfig config) throws IOException, ClassNotFoundException {
        List<Class<? extends IHttpProcessor>> classList = new ArrayList<>();

        // 有配置走配置
        if (!CollectionUtils.isEmpty(config.getPluginJarFilePath())) {
            for (String jarFilePath : config.getPluginJarFilePath()) {
                if (StringUtils.isNotEmpty(jarFilePath)) {
                    File file = new File(jarFilePath);
                    if (!file.exists()) {
                        LOGGER.warn("PluginJarFile not existed! path={}", jarFilePath);
                        continue;
                    }

                    List<Class<? extends IHttpProcessor>> list = ClassLoaderUtil.getAllClass(jarFilePath, IHttpProcessor.class);
                    classList.addAll(list);
                }
            }
        } else {
            // 无配置反射获取当前jar包
            Set<Class<? extends IHttpProcessor>> set = new Reflections(this.getPluginType().getPackageName()).getSubTypesOf(IHttpProcessor.class);
            classList.addAll(set);
        }

        if (CollectionUtils.isEmpty(classList)) {
            LOGGER.warn("No IHttpProcessor need to load!");
            return;
        }

        // registerActor
        classList.forEach(cls -> {
            if (cls.isAnnotationPresent(HttpApiRoute.class)) {
                HttpApiRoute route = cls.getAnnotation(HttpApiRoute.class);
                Class<? extends IHttpProcessor> entry = HttpProcessorMapper.putHttpProcessor(route.apiRouteKey(), cls);
                if (Objects.isNull(entry)) {
                    LOGGER.info("load IHttpProcessor done: {}", cls.getSimpleName());
                }
            }
        });
    }
}
