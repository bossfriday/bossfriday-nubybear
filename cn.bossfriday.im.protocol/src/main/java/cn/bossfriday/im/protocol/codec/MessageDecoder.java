package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.im.protocol.core.MqttMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static cn.bossfriday.im.protocol.core.MqttConstant.FIX_HEADER_LENGTH;
import static cn.bossfriday.im.protocol.core.MqttConstant.MAX_MESSAGE_LENGTH_SIZE;
import static cn.bossfriday.im.protocol.core.MqttException.BAD_MESSAGE_EXCEPTION;
import static cn.bossfriday.im.protocol.core.MqttException.READ_DATA_TIMEOUT_EXCEPTION;

/**
 * MessageDecoder
 *
 * @author chenx
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private final long timeoutMillis;
    private final String userId;
    private final boolean isServer;

    private volatile long lastReadTime;
    private volatile ScheduledFuture<?> timeout;

    private boolean closed;

    public MessageDecoder(long timeoutMillis, String userId, boolean isServer) {
        this.timeoutMillis = timeoutMillis;
        this.userId = userId;
        this.isServer = isServer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = this.decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * readTimedOut
     *
     * @param ctx
     */
    protected void readTimedOut(ChannelHandlerContext ctx) {
        if (!this.closed) {
            this.timeout.cancel(false);
            this.timeout = null;
            this.closed = true;
            ctx.fireExceptionCaught(READ_DATA_TIMEOUT_EXCEPTION);
            ctx.close();
        }
    }

    /**
     * decode
     *
     * @param ctx
     * @param buf
     * @return
     * @throws IOException
     */
    public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (buf.readableBytes() == 0) {
            return null;
        }

        if (buf.readableBytes() < 3) {
            this.resumeTimer(ctx);
            return null;
        }

        buf.markReaderIndex();
        // read away header
        int first = buf.readByte();
        int second = buf.readByte();
        int digit;
        int code = first;
        int msgLength = 0;
        int multiplier = 1;
        int lengthSize = 0;
        do {
            lengthSize++;
            digit = buf.readByte();
            code = code ^ digit;
            msgLength += (digit & 0x7f) * multiplier;
            multiplier *= 128;
            if ((digit & 0x80) > 0 && !buf.isReadable()) {
                this.resumeTimer(ctx);
                buf.resetReaderIndex();
                return null;
            }
        } while ((digit & 0x80) > 0);

        if (code != second) {
            this.close(ctx, buf);
            return null;
        }

        if (lengthSize > MAX_MESSAGE_LENGTH_SIZE) {
            this.close(ctx, buf);
            return null;
        }

        if (buf.readableBytes() < msgLength) {
            this.resumeTimer(ctx);
            buf.resetReaderIndex();
            return null;
        }

        byte[] data = new byte[FIX_HEADER_LENGTH + lengthSize + msgLength];
        buf.resetReaderIndex();
        buf.readBytes(data);
        this.pauseTimer();

        data = MessageObfuscator.obfuscateData(data, FIX_HEADER_LENGTH + lengthSize);
        MqttMessage msg = MessageInputStream.readMessage(new ByteArrayInputStream(data), this.isServer);
        if (msg == null) {
            this.close(ctx, buf);
        }

        return msg;
    }

    /**
     * resumeTimer
     *
     * @param ctx
     */
    private void resumeTimer(ChannelHandlerContext ctx) {
        this.lastReadTime = System.currentTimeMillis();
        if (this.timeoutMillis > 0 && (this.timeout == null || this.timeout.isCancelled()) && !this.closed) {
            this.timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx), this.timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * pauseTimer
     */
    private void pauseTimer() {
        if (this.timeout != null) {
            this.timeout.cancel(false);
        }
    }

    /**
     * close
     */
    private void close(ChannelHandlerContext ctx, ByteBuf buf) {
        if (this.timeout != null) {
            this.timeout.cancel(false);
        }
        this.timeout = null;
        this.closed = true;
        buf.skipBytes(buf.readableBytes());
        ctx.fireExceptionCaught(BAD_MESSAGE_EXCEPTION);
        ctx.close();
    }

    public String getUserId() {
        return this.userId;
    }

    /**
     * ReadTimeoutTask
     */
    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!this.ctx.channel().isOpen()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long nextDelay = MessageDecoder.this.timeoutMillis - (currentTime - MessageDecoder.this.lastReadTime);
            if (nextDelay <= 0) {
                // Read timed out - set a new timeout and notify the callback.
                MessageDecoder.this.timeout = this.ctx.executor().schedule(this, MessageDecoder.this.timeoutMillis, TimeUnit.MILLISECONDS);
                try {
                    MessageDecoder.this.readTimedOut(this.ctx);
                } catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            } else {
                // Read occurred before the timeout - set a new timeout with
                // shorter delay.
                MessageDecoder.this.timeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
