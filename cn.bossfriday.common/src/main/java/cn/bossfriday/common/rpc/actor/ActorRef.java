package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.interfaces.IActorMsgEncoder;
import cn.bossfriday.common.rpc.mailbox.MessageSendBox;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * ActorRef
 *
 * @author chenx
 */
@Slf4j
public class ActorRef {

    private String host;
    private int port;

    @Getter
    private String method;

    @Getter
    private byte[] session;

    private MessageSendBox sendBox;
    private IActorMsgEncoder tellEncoder;
    private ActorSystem actorSystem;
    private BaseUntypedActor callbackActor;
    private long ttl;

    public ActorRef() {

    }

    public ActorRef(String host, int port, byte[] session, ActorSystem actorSystem, BaseUntypedActor callbackActor, long ttl) {
        this.host = host;
        this.port = port;
        this.session = session;
        this.actorSystem = actorSystem;
        this.callbackActor = callbackActor;
        this.ttl = ttl;
        if (this.actorSystem != null) {
            this.sendBox = this.actorSystem.getSendBox();
            this.tellEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    public ActorRef(String host, int port, byte[] session, String method, ActorSystem actorSystem) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.session = session;
        this.actorSystem = actorSystem;
        if (this.actorSystem != null) {
            this.sendBox = this.actorSystem.getSendBox();
            this.tellEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    /**
     * tell
     *
     * @param message
     * @param sender
     */
    public void tell(Object message, ActorRef sender) {
        if (sender == null) {
            throw new BizException("sender is null!");
        }

        if (this.sendBox != null) {
            RpcMessage msg = new RpcMessage();
            msg.setSession(this.session);
            msg.setTargetHost(this.host);
            msg.setTargetPort(this.port);
            msg.setTargetMethod(this.method);
            msg.setSourceHost(sender.host);
            msg.setSourcePort(sender.port);
            msg.setSourceMethod(sender.method);
            msg.setPayloadData(this.tellEncoder.encode(message));

            this.registerCallBackActor(this.session);
            sender.registerCallBackActor(this.session);
            this.sendBox.put(msg);
        }
    }

    /**
     * registerCallBackActor
     *
     * @param session
     */
    public void registerCallBackActor(byte[] session) {
        if (this.callbackActor != null) {
            this.actorSystem.getDispatcher().registerCallBackActor(session, this.callbackActor, this.ttl);
        }
    }

    /**
     * noSender
     *
     * @return
     */
    public static ActorRef noSender() {
        return DeadLetterActorRef.DEAD_LETTER_ACTOR_REF_INSTANCE;
    }
}
