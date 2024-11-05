package cn.bossfriday.im.user;

import cn.bossfriday.common.bootstrap.ServicePluginBootstrap;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * Bootstrap
 * <p>
 * 用户服务
 *
 * @author chenx
 */
@Slf4j
public class Bootstrap extends ServicePluginBootstrap {

    @Override
    protected String getServiceName() {
        return "BossFriday-IM-User";
    }

    @Override
    protected void start() {
        try {
            // ...
        } catch (Exception ex) {
            log.error("Bootstrap.start() error!", ex);
        }
    }

    @Override
    protected void stop() {
        try {
            // ...
        } catch (Exception ex) {
            log.error("Bootstrap.stop() error!", ex);
        }
    }

    /**
     * 本地测试启动入口
     */
    public static void main(String[] args) {
        Bootstrap plugin = new Bootstrap();
        plugin.startup(ConfigurationAllLoader.getInstance().getSystemConfig());
    }
}
