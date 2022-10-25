package cn.bossfriday.common.rpc.mailbox;

import cn.bossfriday.common.rpc.transport.RpcMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * BaseMailBox
 *
 * @author chenx
 */
@Slf4j
public abstract class BaseMailBox {

    protected final LinkedBlockingQueue<RpcMessage> queue;
    protected boolean isStart = true;

    protected BaseMailBox(LinkedBlockingQueue<RpcMessage> queue) {
        this.queue = queue;
    }

    /**
     * start
     */
    public void start() {
        new Thread(() -> {
            while (BaseMailBox.this.isStart) {
                try {
                    RpcMessage msg = BaseMailBox.this.queue.take();
                    BaseMailBox.this.process(msg);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.error("MailBox.process() error!", ex);
                } catch (Exception e) {
                    log.error("MailBox.process() error!", e);
                }
            }
        }).start();
    }

    /**
     * process
     *
     * @param msg
     * @throws Exception
     */
    public abstract void process(RpcMessage msg);

    /**
     * stop
     */
    public abstract void stop();

    /**
     * put
     *
     * @param msg
     */
    public void put(RpcMessage msg) {
        try {
            this.queue.put(msg);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("MailBox.put() error!", ex);
        } catch (Exception ex) {
            log.error("MailBox.put() error!", ex);
        }
    }
}
