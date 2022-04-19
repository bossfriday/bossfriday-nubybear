package cn.bossfriday.common.plugin;

import cn.bossfriday.common.conf.ServiceConfig;

public interface IPlugin {
    /**
     * startup
     */
    void startup(ServiceConfig config);

    /**
     * shutdown
     */
    void shutdown();
}
