package cn.bossfriday.common.http.model;

import cn.bossfriday.common.exception.BizException;
import lombok.Getter;

/**
 * Range
 *
 * @author chenx
 */
@Getter
public class Range {

    /**
     * firstBytePos
     */
    private long firstBytePos;

    /**
     * lastBytePos
     */
    private long lastBytePos;

    public Range(long firstBytePos, long lastBytePos) {
        if (firstBytePos < 0 || lastBytePos < 0) {
            throw new BizException("Range.firstBytePos and Range.lastBytePos must >=0!");
        }

        if (lastBytePos <= firstBytePos) {
            throw new BizException("Range.lastBytePos must > Range.firstBytePos");
        }

        this.firstBytePos = firstBytePos;
        this.lastBytePos = lastBytePos;
    }
}
