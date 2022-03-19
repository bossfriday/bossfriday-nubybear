package cn.bossfriday.common.router;

import cn.bossfriday.common.utils.UUIDUtil;

public class RoutableBeanFactory {
    /**
     * 随机路由
     *
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean buildRandomRouteBean(String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        String routeKey = UUIDUtil.getShortString();
        byte routeType = RouteType.RandomRoute.getValue();

        return new RoutableBean(appId, routeKey, method, null, null, msg, routeType);
    }

    /**
     * 指定Key路由
     *
     * @param routeKey
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean buildKeyRouteBean(String routeKey, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.KeyRoute.getValue();

        return new RoutableBean(appId, routeKey, method, null, null, msg, routeType);
    }

    /**
     * 资源Id路由
     *
     * @param resourceId
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean buildResourceIdRouteBean(String resourceId, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.ResourceIdRoute.getValue();

        return new RoutableBean(appId, null, method, resourceId, null, msg, routeType);
    }

    /**
     * 强制路由
     *
     * @param clusterNodeName
     * @param method
     * @param msg
     * @return
     */
    public static RoutableBean buildForceRouteBean(String clusterNodeName, String method, Object msg) {
        long appId = RoutableBean.DEFAULT_APP_ID;
        byte routeType = RouteType.ForceRoute.getValue();

        return new RoutableBean(appId, null, method, null, clusterNodeName, msg, routeType);
    }
}
