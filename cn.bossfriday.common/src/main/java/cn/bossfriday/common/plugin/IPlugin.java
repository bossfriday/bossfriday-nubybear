package cn.bossfriday.common.plugin;

public interface IPlugin {
    /**
     * startup
     *
     * @param serviceConfigFilePath
     */
    void startup(String serviceConfigFilePath);

    /**
     * shutdown
     */
    void shutdown();
}
