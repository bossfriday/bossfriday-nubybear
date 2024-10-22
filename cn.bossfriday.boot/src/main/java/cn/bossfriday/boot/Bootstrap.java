package cn.bossfriday.boot;

import cn.bossfriday.common.conf.SystemConfig;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.plugin.IPlugin;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
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
        String startupMethodName = getStartupMethodName();
        if (StringUtils.isEmpty(startupMethodName)) {
            throw new ServiceRuntimeException("IPlugin.startup(SystemConfig config) method not found!");
        }

        Set<Class<? extends IPlugin>> pluginClassSet = new Reflections().getSubTypesOf(IPlugin.class);

        for (Class<? extends IPlugin> pluginClass : pluginClassSet) {
            if (Modifier.isAbstract(pluginClass.getModifiers())) {
                // 排除抽象类接口实现
                continue;
            }

            IPlugin instance = pluginClass.getDeclaredConstructor().newInstance();
            Method startupMethod = pluginClass.getMethod(startupMethodName, SystemConfig.class);

            startupMethod.invoke(instance, SystemConfigLoader.getInstance().getSystemConfig());
        }
    }

    /**
     * getStartupMethodName
     */
    private static String getStartupMethodName() {
        Class<IPlugin> clazz = IPlugin.class;
        Method[] methods = clazz.getDeclaredMethods();
        Method startupMethod = Arrays.stream(methods)
                .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == SystemConfig.class)
                .findFirst()
                .orElse(null);

        return Objects.isNull(startupMethod) ? null : startupMethod.getName();
    }
}
