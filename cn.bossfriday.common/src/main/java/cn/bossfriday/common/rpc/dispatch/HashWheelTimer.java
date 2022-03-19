package cn.bossfriday.common.rpc.dispatch;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class HashWheelTimer {
    /**
     * HashedWheelTimer
     */
    private final static Timer timer = new HashedWheelTimer();

    /**
     * putTimeOutTask
     */
    public static void putTimeOutTask(TimerTask task, long time, TimeUnit timeUnit) {
        timer.newTimeout(task, time, timeUnit);
    }
}
