package cn.bossfriday.im.protocol.enums;


import org.apache.commons.lang3.StringUtils;

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
     * Android
     */
    ANDROID("Android"),

    /**
     * PC
     */
    PC("PC"),

    /**
     * MiniProgram
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
        if (StringUtils.isEmpty(platform)) {
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
