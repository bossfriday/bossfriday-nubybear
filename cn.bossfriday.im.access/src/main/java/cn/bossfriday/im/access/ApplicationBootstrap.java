package cn.bossfriday.im.access;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.im.access.common.conf.ImAccessConfigLoader;
import cn.bossfriday.im.access.server.MqttAccessServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * ApplicationBootstrap
 *
 * @author chenx
 */
@Slf4j
public class ApplicationBootstrap extends AbstractServiceBootstrap {

    private MqttAccessServer mqttAccessServer = null;

    @Override
    protected void start() {
        try {
            int port = ImAccessConfigLoader.getConfig().getTcpPort();
            this.mqttAccessServer = new MqttAccessServer(port, null);
            this.mqttAccessServer.start();
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
            if (Objects.nonNull(this.mqttAccessServer)) {
                this.mqttAccessServer.stop();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.stop() error!", ex);
        }
    }


    /**
     * 本地测试启动入口
     */
    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(ImAccessConfigLoader.getSystemConfig());
    }
}
