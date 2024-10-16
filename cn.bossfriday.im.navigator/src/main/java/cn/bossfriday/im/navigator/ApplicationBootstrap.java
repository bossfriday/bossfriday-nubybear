package cn.bossfriday.im.navigator;

import cn.bossfriday.common.PluginBootstrap;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.plugin.PluginType;
import lombok.extern.slf4j.Slf4j;

/**
 * NavigatorBootstrap
 * <p>
 * 导航服务：负责客户端接入地址及全局配置下发；
 *
 * @author chenx
 */
@Slf4j
public class ApplicationBootstrap extends PluginBootstrap {

    @Override
    protected PluginType getPluginType() {
        return PluginType.IM_NAVIGATOR;
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
