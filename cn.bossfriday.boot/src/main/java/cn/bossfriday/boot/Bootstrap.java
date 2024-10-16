package cn.bossfriday.boot;

import cn.bossfriday.common.PluginBootstrap;
import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.conf.SystemConfigLoader;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Bootstrap
 *
 * @author chenx
 */
public class Bootstrap {

    /**
     * 1.启动集群节点内的所有服务；
     * 2.各个服务中的Bootstrap只启动当前服务；
     */
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Set<Class<? extends PluginBootstrap>> pluginBootstrapSet = new Reflections().getSubTypesOf(PluginBootstrap.class);

        for (Class<? extends PluginBootstrap> clazz : pluginBootstrapSet) {
            PluginBootstrap instance = clazz.getDeclaredConstructor().newInstance();
            Method startupMethod = clazz.getMethod("startup", SystemConfig.class);

            startupMethod.invoke(instance, SystemConfigLoader.getInstance().getSystemConfig());
        }
    }
}
