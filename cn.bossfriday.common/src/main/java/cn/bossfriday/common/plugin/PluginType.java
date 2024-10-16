package cn.bossfriday.common.plugin;

import lombok.Getter;

/**
 * PluginType
 *
 * @author chenx
 */
public enum PluginType {
    
    /**
     * 文件服务
     */
    FILE_SERVER("BossFriday-File", "cn.bossfriday.fileserver"),

    /**
     * IM接入服务
     */
    IM_ACCESS("BossFriday-IM-Access", "cn.bossfriday.im.access");

    @Getter
    private String serviceName;

    @Getter
    private String packageName;

    PluginType(String serviceName, String packageName) {
        this.serviceName = serviceName;
        this.packageName = packageName;
    }
}
