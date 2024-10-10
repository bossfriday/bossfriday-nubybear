package cn.bossfriday.im.access.common.enums;

public enum ConnectState {

    /**
     * 连接建立
     */
    CONN(0),

    /**
     * 连接登录失败（向客户端返回了Ack）
     */
    CONN_ACK_FAILURE(1),

    /**
     * 连接登录通过
     */
    CONN_ACK(2),

    /**
     * 连接登录通过-在线
     */
    ONLINE(3),

    /**
     * 离线
     */
    OFFLINE(4),

    /**
     * 注销
     */
    LOGOUT(5),

    /**
     * 关闭连接
     */
    CLOSE(6);

    private int value;

    ConnectState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
