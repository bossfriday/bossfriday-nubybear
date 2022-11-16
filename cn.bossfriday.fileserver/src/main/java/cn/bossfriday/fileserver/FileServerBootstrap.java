package cn.bossfriday.fileserver;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.http.HttpFileServer;
import lombok.extern.slf4j.Slf4j;

/**
 * FileServerBootstrap
 *
 * @author chenx
 */
@Slf4j
public class FileServerBootstrap extends AbstractServiceBootstrap {

    @Override
    protected void start() {
        try {
            StorageHandlerFactory.init();
            StorageEngine.getInstance().start();
            HttpFileServer.start();
        } catch (InterruptedException e) {
            log.error("FileServerBootstrap.start() InterruptedException!", e);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("FileServerBootstrap.start() error!", ex);
        }

    }

    @Override
    protected void stop() {
        try {
            StorageEngine.getInstance().stop();
        } catch (Exception ex) {
            log.error("FileServerBootstrap.stop() error!", ex);
        }
    }

    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new FileServerBootstrap();
        plugin.startup(FileServerConfigManager.getServiceConfig());
    }
}
