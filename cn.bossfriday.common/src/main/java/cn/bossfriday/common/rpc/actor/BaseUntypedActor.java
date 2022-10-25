package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import cn.bossfriday.common.utils.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * BaseUntypedActor
 *
 * @author chenx
 */
@Slf4j
public abstract class BaseUntypedActor {

    @Setter
    @Getter
    private ActorRef sender;

    @Setter
    @Getter
    private ActorRef self;

    /**
     * onReceive
     *
     * @param msg
     */
    public abstract void onReceive(Object msg);

    /**
     * onReceive
     *
     * @param message
     * @param actorSystem
     */
    public void onReceive(RpcMessage message, ActorSystem actorSystem) {
        if (message == null || actorSystem == null) {
            log.warn("UntypedActor.onReceive(msg, actorSystem) returned by msg or actorSystem is null!");
            return;
        }

        this.self = new ActorRef(actorSystem.getSelfAddress().getHostName(), actorSystem.getSelfAddress().getPort(), UUIDUtil.getUuidBytes(), message.getTargetMethod(), actorSystem);
        if (message.hasSource()) {
            if (message.getSourceMethod() == null) {
                // source is callback actor
                this.sender = new ActorRef(message.getSourceHost(), message.getSourcePort(), message.getSession(), actorSystem, null, 0);
            } else {
                this.sender = new ActorRef(message.getSourceHost(), message.getSourcePort(), message.getSession(), message.getSourceMethod(), actorSystem);
            }
        } else {
            this.sender = ActorRef.noSender();
        }

        this.setSender(this.sender);
        this.setSelf(this.self);

        Object msgObj = null;
        try {
            msgObj = actorSystem.getMsgDecoder().decode(message.getPayloadData());
            this.onReceive(msgObj);
        } catch (Throwable throwable) {
            this.onFailed(throwable);
        } finally {
            if (msgObj != null) {
                msgObj = null;
            }

            message = null;
        }
    }

    /**
     * onFailed
     *
     * @param throwable
     */
    public void onFailed(Throwable throwable) {
        if (throwable != null) {
            log.error("UntypedActor.onFailed()", throwable);
        }
    }

    /**
     * onTimeout
     *
     * @param actorKey
     */
    public void onTimeout(String actorKey) {
        log.warn("actor timeout, actorKey:" + actorKey);
    }

    /**
     * clean
     */
    public void clean() {
        this.sender = null;
        this.self = null;
    }
}
