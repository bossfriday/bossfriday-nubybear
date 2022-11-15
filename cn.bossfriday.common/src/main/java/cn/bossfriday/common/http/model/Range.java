package cn.bossfriday.common.http.model;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.MurmurHashUtil;
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

    /**
     * getLength
     *
     * @return
     */
    public long getLength() {
        return this.lastBytePos - this.firstBytePos + 1;
    }

    /**
     * getRangeResponseHeaderValue
     *
     * @param totalSize
     * @return
     */
    public String getRangeResponseHeaderValue(long totalSize) {
        return "bytes=" + this.firstBytePos + "-" + this.lastBytePos + "/" + totalSize;
    }

    @Override
    public int hashCode() {
        return MurmurHashUtil.hash32(this.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Range) {
            Range tmp = (Range) obj;

            return this.firstBytePos == tmp.getFirstBytePos() && this.lastBytePos == tmp.getLastBytePos();
        }

        return false;
    }

    @Override
    public String toString() {
        return this.firstBytePos + "-" + this.lastBytePos;
    }
}
