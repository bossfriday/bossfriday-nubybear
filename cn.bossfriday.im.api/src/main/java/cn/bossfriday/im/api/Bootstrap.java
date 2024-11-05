package cn.bossfriday.im.api;

import cn.bossfriday.common.bootstrap.HttpApiPluginBootstrap;
import cn.bossfriday.im.api.http.HttpApiServer;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
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
public class Bootstrap extends HttpApiPluginBootstrap {

    private HttpApiServer httpApiServer;

    @Override
    protected String getServiceName() {
        return "BossFriday-IM-API";
    }

    @Override
    protected void start() {
        try {
            int port = ConfigurationAllLoader.getInstance().getImApiConfig().getHttpPort();
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
        Bootstrap plugin = new Bootstrap();
        plugin.startup(ConfigurationAllLoader.getInstance().getSystemConfig());
    }
}
