package cn.bossfriday.common.router;

/**
 * RouteType
 *
 * @author chenx
 */
public enum RouteType {

    /**
     * 随机路由
     */
    RANDOM_ROUTE((byte) 0),

    /**
     * 指定Key路由
     */
    KEY_ROUTE((byte) 1),

    /**
     * 资源Id路由
     */
    RESOURCE_ID_ROUTE((byte) 2),

    /**
     * 强制路由
     */
    FORCE_ROUTE((byte) 3);

    private final byte value;

    private RouteType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }

    public int getIntValue() {
        return Byte.toUnsignedInt(this.value);
    }
}
