package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.RecoverableTmpFile;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

import static cn.bossfriday.common.Const.CPU_PROCESSORS;

public abstract class BaseStorageEngine {
    protected Disruptor<RecoverableTmpFileEvent> queue;
    protected RingBuffer<RecoverableTmpFileEvent> ringBuffer;

    protected BaseStorageEngine(int capacity) {
        this.queue = getQueue(capacity);
        queue.handleEventsWithWorkerPool(new RecoverableTmpFileEventHandler());
    }

    /**
     * start
     */
    protected void start() throws Exception {
        ringBuffer = queue.start();

        if (ringBuffer == null)
            throw new Exception("BaseStorageEngine.start() error!");
    }

    /**
     * onRecoverableTmpFileEvent
     */
    protected abstract void onRecoverableTmpFileEvent(RecoverableTmpFile event) throws Exception;

    /**
     * publishEvent
     */
    protected void publishEvent(RecoverableTmpFile msg) {
        EventTranslatorOneArg<RecoverableTmpFileEvent, RecoverableTmpFile> translator = new RecoverableTmpFileEventTranslator();
        ringBuffer.publishEvent(translator, msg);
    }

    public class RecoverableTmpFileEventHandler implements WorkHandler<RecoverableTmpFileEvent> {
        @Override
        public void onEvent(RecoverableTmpFileEvent event) throws Exception {
            onRecoverableTmpFileEvent(event.getMsg());
        }
    }

    public class RecoverableTmpFileEvent {
        private RecoverableTmpFile msg;

        public RecoverableTmpFile getMsg() {
            return msg;
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

    private Disruptor<RecoverableTmpFileEvent> getQueue(int capacity) {
        Disruptor<RecoverableTmpFileEvent> disruptor = new Disruptor<>(
                new RecoverableTmpFileEventFactory(),
                getRingBufferSize(getRingBufferSize(capacity)),
                Executors.newFixedThreadPool(CPU_PROCESSORS),
                ProducerType.MULTI, // ProducerType.SINGLE  ??????????????? ProducerType.MULTI   ????????????
                // BlockingWaitStrategy??????????????????????????????CPU??????????????????
                // SleepingWaitStrategy??????BlockingWaitStrategy????????????????????????????????????????????????
                // YieldingWaitStrategy ????????????????????????????????????????????? CPU ???????????????
                new SleepingWaitStrategy());

        return disruptor;
    }

    /**
     * ??????ringBufferSize?????????2?????????
     */
    private static int getRingBufferSize(int num) {
        int s = 2;
        while (s < num) {
            s <<= 1;
        }

        return s;
    }
}
