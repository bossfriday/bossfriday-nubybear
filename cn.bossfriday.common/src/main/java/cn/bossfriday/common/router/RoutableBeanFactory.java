package cn.bossfriday.common.router;

import cn.bossfriday.common.utils.UUIDUtil;

/**
 * RoutableBeanFactory
 *
 * @author chenx
 */
public class RoutableBeanFactory {

    private RoutableBeanFactory() {

    }

    /**
     * 随机路由
     *
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean<Object> buildRandomRouteBean(String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        String routeKey = UUIDUtil.getShortString();
        byte routeType = RouteType.RANDOM_ROUTE.getValue();

        return new RoutableBean<>(appId, routeKey, method, null, null, msg, routeType);
    }

    /**
     * 指定Key路由
     *
     * @param routeKey
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean<Object> buildKeyRouteBean(String routeKey, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.KEY_ROUTE.getValue();

        return new RoutableBean<>(appId, routeKey, method, null, null, msg, routeType);
    }

    /**
     * 资源Id路由
     *
     * @param resourceId
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean<Object> buildResourceIdRouteBean(String resourceId, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.RESOURCE_ID_ROUTE.getValue();

        return new RoutableBean<>(appId, null, method, resourceId, null, msg, routeType);
    }

    /**
     * 强制路由
     *
     * @param clusterNodeName
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean<Object> buildForceRouteBean(String clusterNodeName, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.FORCE_ROUTE.getValue();

        return new RoutableBean<>(appId, null, method, null, clusterNodeName, msg, routeType);
    }
}
