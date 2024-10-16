package cn.bossfriday.im.api;

import cn.bossfriday.common.PluginBootstrap;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.plugin.PluginType;
import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationBootstrap
 * <p>
 * IM接口服务：对外提供IM系统相关HTTP开放接口
 *
 * @author chenx
 */
@Slf4j
public class ApplicationBootstrap extends PluginBootstrap {

    @Override
    protected PluginType getPluginType() {
        return PluginType.IM_API;
    }

    @Override
    protected void start() {
        // ...
    }

    @Override
    protected void stop() {
        // ...
    }

    /**
     * 本地测试启动入口
     */
    public static void main(String[] args) {
        PluginBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(SystemConfigLoader.getInstance().getSystemConfig());
    }
}
