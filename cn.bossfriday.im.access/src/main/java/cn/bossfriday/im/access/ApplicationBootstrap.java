package cn.bossfriday.im.access;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.common.conf.ServiceConfigManager;
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

    }

    @Override
    protected void stop() {

    }

    /**
     * 本地启动入口
     */
    public static void main(String[] args) {
        AbstractServiceBootstrap plugin = new ApplicationBootstrap();
        plugin.startup(ServiceConfigManager.getServiceConfig());
    }
}
