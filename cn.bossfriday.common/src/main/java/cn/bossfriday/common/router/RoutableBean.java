package cn.bossfriday.common.router;

import lombok.Getter;
import lombok.Setter;

/**
 * RoutableBean
 *
 * @author chenx
 */
public class RoutableBean<T> {

    public static final long DEFAULT_APP_ID = 10000;

    /**
     * 多租户支持预留（默认10000）
     */
    @Getter
    @Setter
    private long appId = DEFAULT_APP_ID;

    @Getter
    private String routeKey;

    @Getter
    private String method;

    @Getter
    private String targetResourceId;

    @Getter
    private String targetClusterNode;

    @Getter
    private T payload;

    @Getter
    private byte routeType;

    public RoutableBean(long appId, String routeKey, String method, String targetResourceId, String targetClusterNode, T payload, byte routeType) {
        this.appId = appId;
        this.routeKey = routeKey;
        this.method = method;
        this.targetResourceId = targetResourceId;
        this.targetClusterNode = targetClusterNode;
        this.payload = payload;
        this.routeType = routeType;
    }
}
