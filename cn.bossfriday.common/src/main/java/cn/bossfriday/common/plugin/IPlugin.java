package cn.bossfriday.common.plugin;

import cn.bossfriday.common.conf.SystemConfig;

/**
 * IPlugin
 *
 * @author chenx
 */
public interface IPlugin {

    /**
     * startup
     *
     * @param config
     */
    void startup(SystemConfig<?> config);

    /**
     * shutdown
     */
    void shutdown();
}
