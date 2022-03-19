package cn.bossfriday.common.router;

public enum RouteType {
    RandomRoute((byte) 0),  // 随机路由

    KeyRoute((byte) 1),   // 指定Key路由

    ResourceIdRoute((byte) 2), // 资源Id路由

    ForceRoute((byte) 3); // 强制路由

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
