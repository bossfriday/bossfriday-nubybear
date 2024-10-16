package cn.bossfriday.common.id;

import cn.bossfriday.common.exception.ServiceRuntimeException;

/**
 * @ClassName: SnowflakeIdWorker
 * @Author: chenx
 */
public class SnowFlakeIdWorker {
    /**
     * 开始时间截 (2021-12-01 08:00:00)
     */
    private static final long TW_EPOCH = 1638316800000L;

    /**
     * 机器id所占的位数
     */
    private static final long WORKER_ID_BITS = 8L;

    /**
     * 数据标识id所占的位数
     */
    private static final long DATACENTER_ID_BITS = 2L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);

    /**
     * 序列在id中占的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID向左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * SnowFlakeIdWorker
     *
     * @param workerId
     * @param datacenterId
     */
    public SnowFlakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }

        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }

        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * nextId (线程安全)
     *
     * @return
     */
    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (timestamp < this.lastTimestamp) {
            throw new ServiceRuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", this.lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1) & SEQUENCE_MASK;
            if (this.sequence == 0) {
                timestamp = this.tillNextMillis(this.lastTimestamp);
            }
        }

        //时间戳改变，毫秒内序列重置
        else {
            this.sequence = 0L;
        }

        //上次生成ID的时间截
        this.lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - TW_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (this.datacenterId << DATACENTER_ID_SHIFT)
                | (this.workerId << WORKER_ID_SHIFT)
                | this.sequence;
    }

    /**
     * tillNextMillis
     *
     * @param lastTimestamp
     * @return
     */
    protected long tillNextMillis(long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }

        return timestamp;
    }

    /**
     * timeGen
     *
     * @return
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
