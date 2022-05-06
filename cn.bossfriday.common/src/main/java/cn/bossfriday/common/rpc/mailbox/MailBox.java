package cn.bossfriday.common.rpc.mailbox;

import cn.bossfriday.common.rpc.transport.RpcMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public abstract class MailBox {
    protected final LinkedBlockingQueue<RpcMessage> queue;
    protected boolean isStart = true;

    public MailBox(LinkedBlockingQueue<RpcMessage> queue) {
        this.queue = queue;
    }

    /**
     * start
     */
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    try {
                        RpcMessage msg = queue.take();
                        process(msg);
                    } catch (Exception e) {
                        log.error("MailBox.process() error!", e);
                    }
                }
            }
        }).start();
    }

    /**
     * process
     */
    public abstract void process(RpcMessage msg) throws Exception;

    /**
     * stop
     */
    public abstract void stop();

    /**
     * put
     */
    public void put(RpcMessage msg) {
        try {
            this.queue.put(msg);
        } catch (Exception e) {
            log.error("MailBox.put() error!", e);
        }
    }
}
