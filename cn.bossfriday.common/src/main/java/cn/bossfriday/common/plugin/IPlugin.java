package cn.bossfriday.common.plugin;

import cn.bossfriday.common.conf.SystemConfig;

/**
 * IPlugin
 *
 * @author chenx
 */
public interface IPlugin {

    /**
     * startup：方法如果改名需要修改Bootstrap中反射调用的方法名称
     *
     * @param config
     */
    void startup(SystemConfig config);

    /**
     * shutdown
     */
    void shutdown();
}
