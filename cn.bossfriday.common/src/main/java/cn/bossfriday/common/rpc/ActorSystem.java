package cn.bossfriday.common.rpc;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.rpc.dispatch.ActorDispatcher;
import cn.bossfriday.common.rpc.interfaces.IActorMsgDecoder;
import cn.bossfriday.common.rpc.interfaces.IActorMsgEncoder;
import cn.bossfriday.common.rpc.mailbox.MessageInBoxBase;
import cn.bossfriday.common.rpc.mailbox.MessageSendBox;
import cn.bossfriday.common.utils.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * ActorSystem
 *
 * @author chenx
 */
@Slf4j
public class ActorSystem {

    @Getter
    private String workerNodeName;

    @Getter
    private InetSocketAddress selfAddress;

    @Getter
    private MessageInBoxBase inBox;

    @Getter
    private MessageSendBox sendBox;

    @Getter
    private ActorDispatcher dispatcher;

    @Getter
    @Setter
    private IActorMsgEncoder msgEncoder;

    @Getter
    @Setter
    private IActorMsgDecoder msgDecoder;

    @Getter
    private boolean isStarted = false;

    private ActorSystem(String workerNodeName, InetSocketAddress selfAddress) {
        this.workerNodeName = workerNodeName;
        this.selfAddress = selfAddress;
        this.dispatcher = new ActorDispatcher(this);
        this.inBox = new MessageInBoxBase(Const.EACH_RECEIVE_QUEUE_SIZE, selfAddress.getPort(), this.dispatcher);
        this.sendBox = new MessageSendBox(this.inBox, selfAddress);
    }

    /**
     * create
     *
     * @param workerNodeName
     * @param selfAddress
     * @return
     */
    public static ActorSystem create(String workerNodeName, InetSocketAddress selfAddress) {
        return new ActorSystem(workerNodeName, selfAddress);
    }

    /**
     * start
     */
    public void start() {
        this.inBox.start();
        this.sendBox.start();
        this.isStarted = true;
    }

    /**
     * stop
     */
    public void stop() {
        this.dispatcher.stop();
        this.sendBox.stop();
        this.inBox.stop();
        this.isStarted = false;
    }

    /**
     * registerActor
     *
     * @param method
     * @param min
     * @param max
     * @param pool
     * @param cls
     * @param args
     */
    public void registerActor(String method, int min, int max, ExecutorService pool, Class<? extends BaseUntypedActor> cls, Object... args) {
        if (StringUtils.isEmpty(method)) {
            throw new BizException("method is null");
        }

        this.dispatcher.registerActor(method, min, max, pool, cls, args);
    }

    /**
     * registerActor
     *
     * @param method
     * @param min
     * @param max
     * @param cls
     * @param args
     */
    public void registerActor(String method, int min, int max, Class<? extends BaseUntypedActor> cls, Object... args) {
        if (StringUtils.isEmpty(method)) {
            throw new BizException("method is null");
        }

        this.dispatcher.registerActor(method, min, max, cls, args);
    }

    /**
     * actorOf(UntypedActor)
     *
     * @param ttl
     * @param cls
     * @param args
     * @return
     */
    public ActorRef actorOf(long ttl, Class<? extends BaseUntypedActor> cls, Object... args) {
        try {
            if (args == null || args.length == 0) {
                return this.actorOf(ttl, cls.newInstance());
            }

            Class<?>[] clsArray = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                clsArray[i] = args[i].getClass();
            }
            Constructor<? extends BaseUntypedActor> constructor = cls.getConstructor(clsArray);
            BaseUntypedActor actor = constructor.newInstance(args);

            return this.actorOf(ttl, actor);
        } catch (Exception e) {
            log.error("ActorSystem.actorOf() error!", e);
        }

        return null;
    }

    /**
     * actorOf(UntypedActor)
     *
     * @param ttl
     * @param actor
     * @return
     */
    public ActorRef actorOf(final long ttl, final BaseUntypedActor actor) {
        return new ActorRef(this.selfAddress.getHostName(), this.selfAddress.getPort(), UUIDUtil.getUuidBytes(), this, actor, ttl);
    }

    /**
     * actorOf(UntypedActor)
     *
     * @param cls
     * @param args
     * @return
     */
    public ActorRef actorOf(Class<? extends BaseUntypedActor> cls, Object... args) {
        return this.actorOf(Const.DEFAULT_CALLBACK_ACTOR_TTL, cls, args);
    }

    /**
     * actorOf(UntypedActor)
     *
     * @param actor
     * @return
     */
    public ActorRef actorOf(BaseUntypedActor actor) {
        return this.actorOf(Const.DEFAULT_CALLBACK_ACTOR_TTL, actor);
    }

    /**
     * actorOf(select ActorRef prepare to tell)
     *
     * @param ip
     * @param port
     * @param targetMethod
     * @return
     */
    public ActorRef actorOf(String ip, int port, String targetMethod) {
        byte[] session = UUIDUtil.toBytes(UUIDUtil.getUuid());
        return new ActorRef(ip, port, session, targetMethod, this);
    }

    /**
     * actorOf(select ActorRef prepare to tell)
     *
     * @param ip
     * @param port
     * @param session
     * @param targetMethod
     * @return
     */
    public ActorRef actorOf(String ip, int port, byte[] session, String targetMethod) {
        return new ActorRef(ip, port, session, targetMethod, this);

    }

    /**
     * actorOf
     *
     * @param session
     * @param targetMethod
     * @return
     */
    public ActorRef actorOf(byte[] session, String targetMethod) {
        return this.actorOf(this.selfAddress.getHostName(), this.selfAddress.getPort(), session, targetMethod);
    }

    /**
     * actorOf
     *
     * @param method
     * @return
     */
    public ActorRef actorOf(String method) {
        return this.actorOf(this.selfAddress.getHostName(), this.selfAddress.getPort(), method);
    }
}
