package cn.bossfriday.im.access;

import cn.bossfriday.common.bootstrap.ServicePluginBootstrap;
import cn.bossfriday.im.access.server.MqttAccessServer;
import cn.bossfriday.im.common.conf.ConfigurationAllLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * ApplicationBootstrap
 * <p>
 * IM接入服务：客户端与该服务建立TCP长连接，协议使用非标MQTT；
 *
 * @author chenx
 */
@Slf4j
public class Bootstrap extends ServicePluginBootstrap {

    private MqttAccessServer mqttAccessServer = null;

    @Override
    protected String getServiceName() {
        return "BossFriday-IM-Access";
    }

    @Override
    protected void start() {
        try {
            int port = ConfigurationAllLoader.getInstance().getImAccessConfig().getMqttPort();
            this.mqttAccessServer = new MqttAccessServer(port);
            this.mqttAccessServer.start();
        } catch (InterruptedException ex) {
            log.error("ServiceBootstrap.start() Interrupted!", ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ServiceBootstrap.start() error!", ex);
        }
    }

    @Override
    protected void stop() {
        try {
            if (Objects.nonNull(this.mqttAccessServer)) {
                this.mqttAccessServer.stop();
            }
        } catch (InterruptedException ex) {
            log.error("ServiceBootstrap.stop() Interrupted!", ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ServiceBootstrap.stop() error!", ex);
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
