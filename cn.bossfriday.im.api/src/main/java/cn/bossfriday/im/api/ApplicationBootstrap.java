package cn.bossfriday.im.api;

import cn.bossfriday.common.PluginBootstrap;
import cn.bossfriday.common.conf.SystemConfigLoader;
import cn.bossfriday.common.plugin.PluginType;
import cn.bossfriday.im.api.http.HttpApiServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * ApplicationBootstrap
 * <p>
 * IM接口服务：对外提供IM系统相关HTTP接口
 *
 * @author chenx
 */
@Slf4j
public class ApplicationBootstrap extends PluginBootstrap {

    private HttpApiServer httpApiServer;

    @Override
    protected PluginType getPluginType() {
        return PluginType.IM_API;
    }

    @Override
    protected void start() {
        try {
            int port = SystemConfigLoader.getInstance().getImApiConfig().getHttpPort();
            this.httpApiServer = new HttpApiServer(port);
            this.httpApiServer.start();
        } catch (InterruptedException ex) {
            log.error("ApplicationBootstrap.start() Interrupted!", ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.start() error!", ex);
        }
    }

    @Override
    protected void stop() {
        try {
            if (Objects.nonNull(this.httpApiServer)) {
                this.httpApiServer.stop();
            }
        } catch (InterruptedException ex) {
            log.error("ApplicationBootstrap.stop() Interrupted!", ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.stop() error!", ex);
        }
    }

    /**
     * 本地测试启动入口
     */
    public static void main(String[] args) {
        PluginBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(SystemConfigLoader.getInstance().getSystemConfig());
    }
}
