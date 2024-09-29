package cn.bossfriday.fileserver;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.common.conf.ServiceConfigManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.http.HttpFileServer;
import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationBootstrap
 *
 * @author chenx
 */
@Slf4j
public class ApplicationBootstrap extends AbstractServiceBootstrap {

    @Override
    protected void start() {
        try {
            StorageHandlerFactory.init();
            StorageEngine.getInstance().start();
            HttpFileServer.getInstance().start();
        } catch (InterruptedException e) {
            log.error("ApplicationBootstrap.start() InterruptedException!", e);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.start() error!", ex);
        }

    }

    @Override
    protected void stop() {
        try {
            StorageEngine.getInstance().stop();
            HttpFileServer.getInstance().stop();
        } catch (InterruptedException e) {
            log.error("ApplicationBootstrap.stop() InterruptedException!", e);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.stop() error!", ex);
        }
    }

    /**
     * 本地启动入口
     */
    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(ServiceConfigManager.getServiceConfig());
    }
}
