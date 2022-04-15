package cn.bossfriday.fileserver;

import cn.bossfriday.common.ServiceBootstrap;

public class Bootstrap extends ServiceBootstrap {
    @Override
    protected void start() throws Exception {

    }

    @Override
    protected void stop() throws Exception {

    }

    public static void main(String[] args) {
        ServiceBootstrap plugin = new Bootstrap();
        plugin.startup("service-config.xml");
    }
}
