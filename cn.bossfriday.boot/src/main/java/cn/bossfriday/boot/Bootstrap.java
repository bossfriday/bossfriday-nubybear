package cn.bossfriday.boot;

import cn.bossfriday.common.common.SystemConfig;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.plugin.IPlugin;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
import cn.bossfriday.im.common.enums.BootstrapBanners;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Bootstrap {

    /**
     * 1.启动集群节点内的所有服务（单独启动某个服务启动各个服务中的Bootstrap即可，例如：cn.bossfriday.fileserver.Bootstrap）；
     * 2.由于只是想做一个IM及文件服务的“脚手架”参考或者二开项目，因此希望系统尽肯能少的去依赖中间件，初步的想法是系统的启动和运行只依赖一个ZK即可，
     * 所有配置归一至：cn.bossfirday.im.common/resources/ConfigurationAll.yaml(ZK配置：System.zkAddress)
     */
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try {
            String startupMethodName = getStartupMethodName();
            if (StringUtils.isEmpty(startupMethodName)) {
                throw new ServiceRuntimeException("IPlugin.startup(SystemConfig config) method not found!");
            }

            Set<Class<? extends IPlugin>> pluginClassSet = new Reflections().getSubTypesOf(IPlugin.class);
            for (Class<? extends IPlugin> pluginClass : pluginClassSet) {
                if (Modifier.isAbstract(pluginClass.getModifiers())) {
                    continue;
                }

                IPlugin instance = pluginClass.getDeclaredConstructor().newInstance();
                Method startupMethod = pluginClass.getMethod(startupMethodName, SystemConfig.class);

                startupMethod.invoke(instance, ConfigurationAllLoader.getInstance().getSystemConfig());
            }

            BootstrapBanners.START.printBanner(log);
        } catch (Exception ex) {
            log.error("Bootstrap error!", ex);
            BootstrapBanners.FAIL.printBanner(log);
        }
    }

    /**
     * getStartupMethodName
     * <p>
     * 目的只是单纯的不想HardCode服务启动方法名，因为方法一旦rename这里将不能work，
     * 因此使用反射+过滤的方式获取IPlugin.startup(SystemConfig config)方法名称；
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
