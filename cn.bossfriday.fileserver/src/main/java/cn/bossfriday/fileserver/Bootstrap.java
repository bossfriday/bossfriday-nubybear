package cn.bossfriday.fileserver;

import cn.bossfriday.common.ServiceBootstrap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.http.HttpFileServer;

public class Bootstrap extends ServiceBootstrap {

    @Override
    protected void start() throws Exception {
        StorageHandlerFactory.init();
        HttpFileServer.start();
    }

    @Override
    protected void stop() throws Exception {

    }

    public static void main(String[] args) {
        ServiceBootstrap plugin = new Bootstrap();
        plugin.startup(FileServerConfigManager.getServiceConfig());
    }
}
