package cn.bossfriday.im.protocol.enums;

/**
 * ConnectionStatus
 *
 * @author chenx
 */
public enum ConnectionStatus {

    /**
     * 连接成功
     */
    ACCEPTED(0),

    /**
     * UNACCEPTABLE_PROTOCOL_VERSION
     */
    UNACCEPTABLE_PROTOCOL_VERSION(1),

    /**
     * appKey转化appId失败，appId在数据库没有配置
     */
    IDENTIFIER_REJECTED(2),

    /**
     * PC or MINI 配置不允许登录
     */
    SERVER_UNAVAILABLE(3),

    /**
     * token 没有userId, 解析失败
     */
    BAD_USERNAME_OR_PASSWORD(4),

    /**
     * 防黑产
     */
    NOT_AUTHORIZED(5),

    /**
     * 客户端接入服务落点错误。
     * 集群内有接入服务主机掉出集群，相邻节点节点主机会有部分请求引发这种错误；或者客户端缓存了错误的接入地址。
     */
    REDIRECT(6),

    /**
     * PACKAGE_ERROR
     */
    PACKAGE_ERROR(7),

    /**
     * App不可用
     */
    APP_BLOCK_OR_DELETE(8),

    /**
     * 用户封禁
     */
    BLOCK(9),

    /**
     * token过期
     */
    TOKEN_EXPIRE(10),

    /**
     * token deviceId不合法
     */
    DEVICE_ERROR(11),

    /**
     * web端登陆，配置过安全域，请求中的安全域配置和数据库中的配置不相符
     */
    HOSTNAME_ERROR(12),

    /**
     * 多端互踢,存在其他同类型端在线，不能登录
     */
    HAS_OTHER_SAME_CLIENT_ONLINE(13),

    /**
     * 在其他环境，做校验用
     */
    IN_OTHER_CLUSTER(15),

    /**
     * APP_AUTH_NOT_PASS
     */
    APP_AUTH_NOT_PASS(16),

    /**
     * OTP_USED
     */
    OTP_USED(17),

    /**
     * PLATFORM_ERROR
     */
    PLATFORM_ERROR(18),

    /**
     * 用户已注销
     */
    USER_CLOSED_ACCOUNT(19);

    private int value;

    ConnectionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
