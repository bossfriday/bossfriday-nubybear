package cn.bossfriday.common.rpc.mailbox;

import cn.bossfriday.common.rpc.transport.NettyClient;
import cn.bossfriday.common.rpc.transport.RpcMessage;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfriday.common.Const.EACH_SEND_QUEUE_SIZE;

/**
 * MessageSendBox
 *
 * @author chenx
 */
@Slf4j
public class MessageSendBox extends BaseMailBox {

    private MessageInBox inBox;
    private InetSocketAddress selfAddress;
    private ConcurrentHashMap<InetSocketAddress, NettyClient> clientMap = new ConcurrentHashMap<>();

    public MessageSendBox(MessageInBox inBox, InetSocketAddress selfAddress) {
        super(new LinkedBlockingQueue<>(EACH_SEND_QUEUE_SIZE));

        this.inBox = inBox;
        this.selfAddress = selfAddress;
    }

    @Override
    public void process(RpcMessage msg) {
        if (msg != null) {
            InetSocketAddress targetAddress = new InetSocketAddress(msg.getTargetHost(), msg.getTargetPort());

            // 本机通讯：不走网络（直接入接收队列）
            if (this.selfAddress.equals(targetAddress)) {
                this.inBox.put(msg);

                return;
            }

            // 跨机通讯
            if (!this.clientMap.containsKey(targetAddress)) {
                NettyClient client = new NettyClient(msg.getTargetHost(), msg.getTargetPort());
                this.clientMap.putIfAbsent(targetAddress, client);
            }

            this.clientMap.get(targetAddress).send(msg);
        }
    }

    @Override
    public void stop() {
        try {
            super.isStart = false;
            super.queue.clear();

            for (Map.Entry<InetSocketAddress, NettyClient> entry : this.clientMap.entrySet()) {
                entry.getValue().close();
            }

            this.clientMap = new ConcurrentHashMap<>(16);
        } catch (Exception e) {
            log.error("MessageSendBox.stop() error!", e);
        }
    }
}
