package cn.bossfriday.fileserver;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.http.HttpFileServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileServerBootstrapAbstract extends AbstractServiceBootstrap {

    @Override
    protected void start() {
        try {
            StorageHandlerFactory.init();
            StorageEngine.getInstance().start();
            HttpFileServer.start();
        } catch (Exception ex) {
            log.error("FileServerBootstrap.start() error!", ex);
        }

    }

    @Override
    protected void stop() {
        StorageEngine.getInstance().stop();
    }

    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new FileServerBootstrapAbstract();
        plugin.startup(FileServerConfigManager.getServiceConfig());
    }
}
