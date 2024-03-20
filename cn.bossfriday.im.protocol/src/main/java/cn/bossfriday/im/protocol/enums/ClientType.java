package cn.bossfriday.im.protocol.enums;

import org.apache.commons.lang.StringUtils;

/**
 * ClientType
 *
 * @author chenx
 */
public enum ClientType {

    /**
     * iOS
     */
    IOS("iOS"),

    /**
     * 安卓
     */
    ANDROID("Android"),

    /**
     * PC
     */
    PC("PC"),

    /**
     * 微信小程序
     */
    MINI("MiniProgram"),

    /**
     * Websocket
     */
    WS("Websocket");

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
            return null;
        }

        for (ClientType clientType : values()) {
            if (clientType.getPlatform().equalsIgnoreCase(platform)) {
                return clientType;
            }
        }

        return null;
    }
}
