package cn.bossfriday.common.rpc.dispatch;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * HashWheelTimer
 *
 * @author chenx
 */
public class HashWheelTimer {

    /**
     * HashedWheelTimer
     */
    private static final Timer HASHED_WHEEL_TIMER = new HashedWheelTimer();

    private HashWheelTimer() {

    }

    /**
     * putTimeOutTask
     *
     * @param task
     * @param time
     * @param timeUnit
     */
    public static void putTimeOutTask(TimerTask task, long time, TimeUnit timeUnit) {
        HASHED_WHEEL_TIMER.newTimeout(task, time, timeUnit);
    }
}
