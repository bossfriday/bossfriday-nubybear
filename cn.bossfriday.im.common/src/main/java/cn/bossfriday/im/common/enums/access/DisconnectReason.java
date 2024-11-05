package cn.bossfriday.im.common.enums.access;

public enum DisconnectReason {

    /**
     * 连接错误断开，例如：Token error、Redirect、block等)
     */
    CONNECT_ERROR("1"),

    /**
     * 客户端发送DisconnectMessage断开
     */
    CLIENT_DISCONNECT_MESSAGE("2"),

    /**
     * 客户端发送DisconnectMessage断开(logoff)
     */
    CLIENT_DISCONNECT_MESSAGE_LOGOFF("3"),

    /**
     * 客户端Ping超时断开
     */
    CLIENT_PING_TIMEOUT_DISCONNECT("4"),

    /**
     * 服务端解析读包超时断开
     */
    READ_DATA_TIMEOUT_DISCONNECT("5"),

    /**
     * 非法协议包断开
     */
    BAD_MESSAGE("6"),

    /**
     * 异常断开
     */
    EXCEPTION_DISCONNECT("7"),

    ;

    private String value;

    DisconnectReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}