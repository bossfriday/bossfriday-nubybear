package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.router.ClusterRouter;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.rpc.ActorSystem;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BaseActor
 * <p>
 * R: RequestMsgType
 * C: ActorContextType
 *
 * @author chenx
 */
public abstract class BaseActor<R, C> extends BaseUntypedActor {

    @Getter
    protected C context;

    @Getter
    protected Class<R> requestType;

    protected BaseActor() {
        Type superclass = this.getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            this.requestType = (Class<R>) parameterized.getActualTypeArguments()[0];
        }
    }

    /**
     * onMessageReceived
     *
     * @param msg
     */
    public abstract void onMessageReceived(R msg);

    @Override
    public void onMsgReceive(Object msg) {
        this.onMessageReceived((R) msg);
    }

    /**
     * getClusterRouter
     *
     * @return
     */
    protected ClusterRouter getClusterRouter() {
        return ClusterRouterFactory.getClusterRouter();
    }

    /**
     * getActorSystem
     */
    protected ActorSystem getActorSystem() {
        return ClusterRouterFactory.getClusterRouter().getActorSystem();
    }

    /**
     * routeMessage
     *
     * @param routeMsg
     * @param sender
     * @return
     */
    protected final String routeMessage(RoutableBean<Object> routeMsg, ActorRef sender) {
        return ClusterRouterFactory.getClusterRouter().routeMessage(routeMsg, sender);
    }
}
