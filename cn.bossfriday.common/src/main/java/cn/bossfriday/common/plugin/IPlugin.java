package cn.bossfriday.common.plugin;

import cn.bossfriday.common.conf.ServiceConfig;

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
    void startup(ServiceConfig config);

    /**
     * shutdown
     */
    void shutdown();
}
