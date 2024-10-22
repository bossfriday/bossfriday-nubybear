package cn.bossfriday.boot;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.plugin.IPlugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static cn.bossfriday.common.plugin.PluginType.PLUGIN_STARTUP_METHOD_NAME;

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
        Set<Class<? extends IPlugin>> pluginClassSet = new Reflections().getSubTypesOf(IPlugin.class);

        for (Class<? extends IPlugin> pluginClass : pluginClassSet) {
            if (Modifier.isAbstract(pluginClass.getModifiers())) {
                // 排除抽象类接口实现
                continue;
            }

            IPlugin instance = pluginClass.getDeclaredConstructor().newInstance();
            Method startupMethod = pluginClass.getMethod(PLUGIN_STARTUP_METHOD_NAME, SystemConfig.class);

            startupMethod.invoke(instance, SystemConfigLoader.getInstance().getSystemConfig());
        }
    }
}
