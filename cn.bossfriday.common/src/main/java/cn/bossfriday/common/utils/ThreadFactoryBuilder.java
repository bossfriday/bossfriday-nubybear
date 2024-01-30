package cn.bossfriday.common.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ThreadFactoryBuilder
 *
 * @author chenx
 */
public final class ThreadFactoryBuilder {

    private String nameFormat = null;
    private Boolean daemon = null;
    private Integer priority = null;
    private UncaughtExceptionHandler uncaughtExceptionHandler = null;
    private ThreadFactory backingThreadFactory = null;

    public ThreadFactoryBuilder() {
        // just an empty constructor
    }

    /**
     * setNameFormat
     *
     * @param nameFormat
     * @return
     */
    public ThreadFactoryBuilder setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;

        return this;
    }

    /**
     * setDaemon
     *
     * @param daemon
     * @return
     */
    public ThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * setPriority
     *
     * @param priority
     * @return
     */
    public ThreadFactoryBuilder setPriority(int priority) {
        this.checkArgument(priority >= Thread.MIN_PRIORITY,
                "Thread priority (%s) must be >= %s", priority, Thread.MIN_PRIORITY);
        this.checkArgument(priority <= Thread.MAX_PRIORITY,
                "Thread priority (%s) must be <= %s", priority, Thread.MAX_PRIORITY);
        this.priority = priority;
        return this;
    }

    /**
     * checkNotNull
     *
     * @param reference
     * @param <T>
     * @return
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }

        return reference;
    }

    /**
     * checkNotNull
     *
     * @param reference
     * @param errorMessage
     * @param <T>
     * @return
     */
    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }

        return reference;
    }


    /**
     * Sets the {@link UncaughtExceptionHandler} for new threads created with this ThreadFactory.
     *
     * @param uncaughtExceptionHandler the uncaught exception handler for new Threads created with this ThreadFactory
     * @return this for the builder pattern
     */
    public ThreadFactoryBuilder setUncaughtExceptionHandler(
            UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = checkNotNull(uncaughtExceptionHandler);

        return this;
    }

    public ThreadFactoryBuilder setThreadFactory(
            ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = checkNotNull(backingThreadFactory);

        return this;
    }

    /**
     * Returns a new thread factory using the options supplied during the building process. After building, it is still
     * possible to change the options used to build the ThreadFactory and/or build again. State is not shared amongst
     * built instances.
     *
     * @return the fully constructed {@link ThreadFactory}
     */
    public ThreadFactory build() {
        return build(this);
    }

    /**
     * build
     *
     * @param builder
     * @return
     */
    @SuppressWarnings("squid:S1604")
    private static ThreadFactory build(ThreadFactoryBuilder builder) {
        final String nameFormat = builder.nameFormat;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final UncaughtExceptionHandler uncaughtExceptionHandler =
                builder.uncaughtExceptionHandler;
        final ThreadFactory backingThreadFactory =
                (builder.backingThreadFactory != null)
                        ? builder.backingThreadFactory
                        : Executors.defaultThreadFactory();
        final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = backingThreadFactory.newThread(runnable);
                if (nameFormat != null) {
                    thread.setName(String.format(nameFormat, count.getAndIncrement()));
                }
                if (daemon != null) {
                    thread.setDaemon(daemon);
                }
                if (priority != null) {
                    thread.setPriority(priority);
                }
                if (uncaughtExceptionHandler != null) {
                    thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                }

                return thread;
            }
        };
    }

    /**
     * checkArgument
     *
     * @param expression
     * @param errMsgTemplate
     * @param errorMessageArgs
     */
    private void checkArgument(boolean expression, String errMsgTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errMsgTemplate, errorMessageArgs));
        }
    }
}
