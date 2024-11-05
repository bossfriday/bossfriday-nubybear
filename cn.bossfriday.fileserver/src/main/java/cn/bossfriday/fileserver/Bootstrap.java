package cn.bossfriday.fileserver;

import cn.bossfriday.common.bootstrap.ServicePluginBootstrap;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.StorageHandlerFactory;
import cn.bossfriday.fileserver.http.HttpFileServer;
import cn.bossfriday.im.common.conf.SystemConfigLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * ServiceBootstrap
 * <p>
 * 文件服务：负责文件上传和下载；
 *
 * @author chenx
 */
@Slf4j
public class Bootstrap extends ServicePluginBootstrap {

    @Override
    protected String getServiceName() {
        return "BossFriday-File-Sever";
    }

    @Override
    protected void start() {
        try {
            StorageHandlerFactory.init();
            StorageEngine.getInstance().start();
            HttpFileServer.getInstance().start();
        } catch (InterruptedException e) {
            log.error("ServiceBootstrap.start() InterruptedException!", e);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ServiceBootstrap.start() error!", ex);
        }
    }

    @Override
    protected void stop() {
        try {
            StorageEngine.getInstance().stop();
            HttpFileServer.getInstance().stop();
        } catch (InterruptedException e) {
            log.error("ServiceBootstrap.stop() InterruptedException!", e);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("ServiceBootstrap.stop() error!", ex);
        }
    }

    /**
     * 本地启动入口
     */
    public static void main(String[] args) {
        Bootstrap plugin = new Bootstrap();
        plugin.startup(SystemConfigLoader.getInstance().getSystemConfig());
    }
}
