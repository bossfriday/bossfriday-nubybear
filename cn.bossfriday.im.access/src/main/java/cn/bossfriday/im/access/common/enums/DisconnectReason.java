package cn.bossfriday.im.access.common.enums;

public enum DisconnectReason {

    /**
     * 链接断开(例如：Token error、Redirect、block等)
     */
    CONNECT_CLOSE("1"),

    /**
     * SDK发送 DisconnectMessage
     */
    SDK_DISCONNECT_MESSAGE("2"),

    /**
     * SDK发送 DisconnectMessage logoff
     */
    SDK_DISCONNECT_MESSAGE_LOGOFF("3"),

    /**
     * SDK ping超时
     */
    SDK_PING_TIMEOUT_DISCONNECT("4"),

    /**
     * 服务端解析读包超时
     */
    READ_DATA_TIMEOUT_DISCONNECT("5"),

    /**
     * 非法协议包 断链接
     */
    BAD_MESSAGE("6"),

    /**
     * 其他异常断链接
     */
    EXCEPTION_DISCONNECT("7"),

    /**
     * 其他设备登录
     */
    OTHER_DEVICE_LOGIN("8"),

    /**
     * 当非强制踢相同类型端时，关闭本端
     */
    CLOSE_SELF("9");

    private String value;

    private DisconnectReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}