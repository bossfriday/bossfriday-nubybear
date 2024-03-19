package cn.bossfriday.im.protocol;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * MessageCallback
 *
 * @author chenx
 */
public abstract class MessageCallback {

    protected final long timeoutMillis;

    protected volatile long lastReadTime;

    protected String userId;

    protected long readTime;

    private volatile ScheduledFuture<?> timeout;

    protected MessageCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    protected MessageCallback(String userId) {
        this();
        this.userId = userId;
    }

    protected MessageCallback() {
        this(30000);
    }

    /**
     * resumeTimer
     *
     * @param ctx
     */
    public void resumeTimer(ChannelHandlerContext ctx) {
        this.lastReadTime = System.currentTimeMillis();
        if (this.timeoutMillis > 0 && (this.timeout == null || this.timeout.isCancelled())) {
            this.timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx), this.timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * pauseTimer
     */
    public void pauseTimer() {
        if (this.timeout != null) {
            this.timeout.cancel(false);
            this.timeout = null;
        }
    }

    /**
     * Is called when a read timeout was detected.
     */
    protected void readTimedOut(ChannelHandlerContext ctx) {
        this.pauseTimer();
    }

    /**
     * ReadTimeoutTask
     */
    private class ReadTimeoutTask implements Runnable {

        private ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!this.ctx.channel().isOpen()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long nextDelay = MessageCallback.this.timeoutMillis - (currentTime - MessageCallback.this.lastReadTime);
            if (nextDelay <= 0) {
                try {
                    MessageCallback.this.readTimedOut(this.ctx);
                } catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            } else {
                MessageCallback.this.timeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    public String getUserId() {
        return this.userId;
    }
}
