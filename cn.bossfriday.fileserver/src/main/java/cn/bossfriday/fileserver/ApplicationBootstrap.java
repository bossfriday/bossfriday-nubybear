package cn.bossfriday.fileserver;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
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
            log.info("=================================");
            log.info("cn.bossfriday.fileserver started.");
            log.info("=================================");
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
        } catch (Exception ex) {
            log.error("ApplicationBootstrap.stop() error!", ex);
        }
    }

    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(FileServerConfigManager.getServiceConfig());
    }
}
