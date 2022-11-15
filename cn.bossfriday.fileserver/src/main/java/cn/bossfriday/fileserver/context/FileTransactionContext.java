package cn.bossfriday.fileserver.context;

import cn.bossfriday.common.combo.Combo2;
import cn.bossfriday.common.http.model.Range;
import io.netty.channel.ChannelHandlerContext;
import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FileTransactionContext
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileTransactionContext {

    private String fileTransactionId;

    private ChannelHandlerContext ctx;

    private boolean isKeepAlive;

    private String userAgent;

    private AtomicLong tempFileSavedCounter = new AtomicLong(0);

    /**
     * key: Range to String
     * value: the savedCounter of the range
     */
    private Map<String, AtomicLong> rangeSavedCounterMap = new ConcurrentHashMap<>(16);

    /**
     * addAndGetTempFileWriteIndex
     *
     * @param size
     * @return
     */
    public Long addAndGetTempFileWriteIndex(int size) {
        return this.tempFileSavedCounter.addAndGet(size);
    }

    /**
     * addAndGetTempFileSavedCounter
     * 由于处理中保障了FileTransactionId与其处理线程的一致性，因此这里不用加锁
     *
     * @param size
     * @param range
     * @return V1: rangeSavedCounterValue, V2: tempFileSavedCounterValue
     */
    public Combo2<Long, Long> addAndGetTempFileSavedCounter(int size, Range range) {
        // 全量上传
        if (range == null) {
            return new Combo2<>(0L, this.tempFileSavedCounter.addAndGet(size));
        }

        // 断点上传
        AtomicLong rangeSavedCounter = this.getRangeSavedCounter(range.toString());
        
        return new Combo2<>(rangeSavedCounter.addAndGet(size), this.tempFileSavedCounter.addAndGet(size));
    }

    /**
     * getRangeSavedCounter
     *
     * @param key
     * @return
     */
    private AtomicLong getRangeSavedCounter(String key) {
        if (this.rangeSavedCounterMap.containsKey(key)) {
            return this.rangeSavedCounterMap.get(key);
        }

        this.rangeSavedCounterMap.put(key, new AtomicLong(0));
        return this.rangeSavedCounterMap.get(key);
    }
}
