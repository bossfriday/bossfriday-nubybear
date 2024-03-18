package cn.bossfriday.im.protocol.enums;

import org.apache.commons.lang.StringUtils;

/**
 * ClientType
 *
 * @author chenx
 */
public enum ClientType {

    /**
     * ios终端
     */
    IOS("iOS"),

    /**
     * 安卓终端
     */
    ANDROID("Android"),

    /**
     * pc终端
     */
    PC("PC"),

    /**
     * 微信小程序模拟器
     */
    MINI("MiniProgram"),

    /**
     * websocket模拟器
     */
    WS("Websocket"),

    /**
     * 没有归属的平台类型
     */
    DEFAULT("error"),

    /**
     * iso另起一个进程，只做拉消息的业务平台类型
     * 跳过多端检查，都可以登录
     * 查询在线状态时，忽略这个平台类型
     */
    IPUSH("IPUSH");

    private final String platform;

    ClientType(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return this.platform;
    }

    /**
     * getClientType
     *
     * @param platform
     * @return
     */
    public static ClientType getClientType(String platform) {
        if (StringUtils.isBlank(platform)) {
            return DEFAULT;
        }

        for (ClientType clientType : values()) {
            if (clientType.getPlatform().equalsIgnoreCase(platform)) {
                return clientType;
            }
        }

        return DEFAULT;
    }
}
