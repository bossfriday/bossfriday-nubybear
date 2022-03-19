package cn.bossfriday.mocks.rpc;

import cn.bossfriday.common.ServiceBootstrap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bootstrap extends ServiceBootstrap {
    @Override
    protected void start() throws Exception {

    }

    @Override
    protected void stop() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        ServiceBootstrap plugin = new Bootstrap();
        plugin.startup("service-config.xml");
    }
}
