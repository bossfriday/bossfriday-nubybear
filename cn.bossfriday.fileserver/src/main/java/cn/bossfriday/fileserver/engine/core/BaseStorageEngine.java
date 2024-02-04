package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.ThreadFactoryBuilder;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.engine.model.RecoverableTmpFile;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

/**
 * BaseStorageEngine
 *
 * @author chenx
 */
@Slf4j
public abstract class BaseStorageEngine {

    protected Disruptor<RecoverableTmpFileEvent> queue;
    protected RingBuffer<RecoverableTmpFileEvent> ringBuffer;

    protected BaseStorageEngine(int capacity) {
        this.queue = this.getQueue(capacity);
        this.queue.handleEventsWithWorkerPool(new RecoverableTmpFileEventHandler());
    }

    /**
     * start
     */
    public void start() {
        this.ringBuffer = this.queue.start();

        if (this.ringBuffer == null) {
            throw new ServiceRuntimeException("BaseStorageEngine.start() error!");
        }

        this.startup();
        log.info("StorageEngine startup() done - " + FileServerConfigManager.getCurrentClusterNodeName());
    }

    /**
     * stop
     */
    public void stop() {
        this.queue.shutdown();
        this.shutdown();
        log.info("StorageEngine stop() done - " + FileServerConfigManager.getCurrentClusterNodeName());
    }

    /**
     * startup
     */
    protected abstract void startup();

    /**
     * shutdown
     */
    protected abstract void shutdown();

    /**
     * onRecoverableTmpFileEvent
     *
     * @param event
     */
    protected abstract void onRecoverableTmpFileEvent(RecoverableTmpFile event);

    /**
     * publishEvent
     *
     * @param msg
     */
    protected void publishEvent(RecoverableTmpFile msg) {
        EventTranslatorOneArg<RecoverableTmpFileEvent, RecoverableTmpFile> translator = new RecoverableTmpFileEventTranslator();
        this.ringBuffer.publishEvent(translator, msg);
    }

    /**
     * RecoverableTmpFileEventHandler
     */
    public class RecoverableTmpFileEventHandler implements WorkHandler<RecoverableTmpFileEvent> {

        @Override
        public void onEvent(RecoverableTmpFileEvent event) throws Exception {
            BaseStorageEngine.this.onRecoverableTmpFileEvent(event.getMsg());
        }
    }

    public class RecoverableTmpFileEvent {

        private RecoverableTmpFile msg;

        public RecoverableTmpFile getMsg() {
            return this.msg;
        }

        public void setMsg(RecoverableTmpFile msg) {
            this.msg = msg;
        }
    }

    public class RecoverableTmpFileEventFactory implements EventFactory<RecoverableTmpFileEvent> {

        @Override
        public RecoverableTmpFileEvent newInstance() {
            return new RecoverableTmpFileEvent();
        }
    }

    public class RecoverableTmpFileEventTranslator implements EventTranslatorOneArg<RecoverableTmpFileEvent, RecoverableTmpFile> {

        @Override
        public void translateTo(RecoverableTmpFileEvent event, long l, RecoverableTmpFile msg) {
            event.setMsg(msg);
        }
    }

    /**
     * getQueue
     *
     * @param capacity
     * @return
     */
    private Disruptor<RecoverableTmpFileEvent> getQueue(int capacity) {
        return new Disruptor<>(
                new RecoverableTmpFileEventFactory(),
                getRingBufferSize(getRingBufferSize(capacity)),
                new ThreadFactoryBuilder().setNameFormat("BaseStorageEngine-Disruptor-%d").setDaemon(true).build(),
                /**
                 * ProducerType.SINGLE：单生产者
                 * ProducerType.MULTI：多生产者
                 */
                ProducerType.MULTI,
                /**
                 * BlockingWaitStrategy：最低效的策略，但对CPU的消耗最小；
                 * SleepingWaitStrategy：与BlockingWaitStrategy类似，合用于异步日志类似的场景；
                 * YieldingWaitStrategy：性能最好，要求事件处理线数小于 CPU 逻辑核心数
                 */
                new SleepingWaitStrategy()
        );
    }

    /**
     * getRingBufferSize(保障ringBufferSize一定为2的次方)
     *
     * @param num
     * @return
     */
    private static int getRingBufferSize(int num) {
        int s = 2;
        while (s < num) {
            s <<= 1;
        }

        return s;
    }
}
