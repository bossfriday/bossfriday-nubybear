package cn.bossfriday.im.common.rpc;

import cn.bossfriday.common.router.ClusterRouter;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.im.common.rpc.message.ApiRequest;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BaseActor
 * <p>
 * T: Input
 *
 * @author chenx
 */
public abstract class BaseActor<T> extends BaseUntypedActor {

    @Getter
    protected ActorContext context;

    @Getter
    protected Class<T> requestType;

    protected BaseActor() {
        Type superclass = this.getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            this.requestType = (Class<T>) parameterized.getActualTypeArguments()[0];
        }
    }

    /**
     * onMessageReceived
     *
     * @param msg
     */
    public abstract void onMessageReceived(T msg);

    @Override
    public void onMsgReceive(Object msg) {
        T request = null;

        if (msg.getClass() == this.requestType) {
            request = (T) msg;
        } else if (msg instanceof ApiRequest) {
            this.context = ((ApiRequest) msg).getActorContext();
        }

        this.onMessageReceived(request);
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
