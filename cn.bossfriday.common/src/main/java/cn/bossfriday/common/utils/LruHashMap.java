package cn.bossfriday.common.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LruHashMap
 *
 * @author chenx
 */
public class LruHashMap<K, V> {

    private static ScheduledExecutorService expireExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("lru-schedule-pool-%d")
                    .setDaemon(true)
                    .build()
    );

    private AtomicBoolean isCleanerRunning = new AtomicBoolean(false);

    private long duration = -1;

    private LruContainerMap<K, TimestampEntryValue<V>> container;

    private Runnable expireRunnable = new Runnable() {

        @Override
        public void run() {
            long nextInterval = 1000;
            LruHashMap.this.container.getLock().lock();
            try {
                boolean shouldStopCleaner = true;
                if (LruHashMap.this.container.size() > 0) {
                    long now = System.currentTimeMillis();
                    List<K> toBeRemoved = new ArrayList<>();
                    for (Map.Entry<K, TimestampEntryValue<V>> e : LruHashMap.this.container.entrySet()) {
                        K key = e.getKey();
                        TimestampEntryValue<V> tValue = e.getValue();
                        long timeLapsed = now - tValue.timestamp;
                        if (timeLapsed >= LruHashMap.this.duration) {
                            toBeRemoved.add(key);
                        } else {
                            long delta = LruHashMap.this.duration - timeLapsed;
                            if (delta > 1000L) {
                                nextInterval = delta;
                            }
                            break;
                        }
                    }

                    if (!CollectionUtils.isEmpty(toBeRemoved)) {
                        for (K key : toBeRemoved) {
                            LruHashMap.this.container.remove(key);
                        }
                    }

                    if (LruHashMap.this.container.size() > 0) {
                        shouldStopCleaner = false;
                    }
                }

                if (shouldStopCleaner) {
                    LruHashMap.this.isCleanerRunning.compareAndSet(true, false);
                } else {
                    expireExecutor.schedule(this, nextInterval, TimeUnit.MILLISECONDS);
                }

            } finally {
                LruHashMap.this.container.getLock().unlock();
            }
        }
    };

    public LruHashMap(int maxSize, Func.Action2<K, V> onEvict) {
        this(maxSize, onEvict, -1L);
    }

    public LruHashMap(int maxSize, Func.Action2<K, V> onEvict, long duration) {
        Func.Action2<K, TimestampEntryValue<V>> doOnEvict = null;
        if (onEvict != null) {
            doOnEvict = (key, value) -> {
                if (value != null) {
                    onEvict.invoke(key, value.value);
                }
            };
        }

        this.duration = duration;
        this.container = new LruContainerMap<>(maxSize, doOnEvict);
    }

    /**
     * size
     *
     * @return
     */
    public int size() {
        return this.container.size();
    }

    /**
     * getKeys
     *
     * @return
     */
    public Set<K> getKeys() {
        return this.container.keySet();
    }

    /**
     * put
     *
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = this.container.put(key, v);
        if (this.duration > 0 && this.isCleanerRunning.compareAndSet(false, true)) {
            expireExecutor.schedule(this.expireRunnable, this.duration, TimeUnit.MILLISECONDS);
        }

        return old == null ? null : old.value;
    }

    /**
     * putIfAbsent
     *
     * @param key
     * @param value
     * @return
     */
    public V putIfAbsent(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = this.container.putIfAbsent(key, v);

        boolean isSchedule = (old == null)
                && this.duration > 0
                && this.isCleanerRunning.compareAndSet(false, true);
        if (isSchedule) {
            expireExecutor.schedule(this.expireRunnable, this.duration, TimeUnit.MILLISECONDS);
        }

        return old == null ? null : old.value;
    }

    /**
     * containsKey
     *
     * @param key
     * @return
     */
    public boolean containsKey(Object key) {
        return this.container.containsKey(key);
    }

    /**
     * get
     *
     * @param key
     * @return
     */
    public V get(Object key) {
        TimestampEntryValue<V> got = this.container.get(key);
        V ret = null;
        if (got != null) {
            got.timestamp = System.currentTimeMillis();
            ret = got.value;
        }

        return ret;
    }

    /**
     * remove
     *
     * @param key
     * @param doEvict
     * @return
     */
    public V remove(Object key, boolean doEvict) {
        TimestampEntryValue<V> removed;
        if (doEvict) {
            removed = this.container.remove(key);
        } else {
            removed = this.container.removeUnEvict(key);
        }

        V ret = null;
        if (removed != null) {
            ret = removed.value;
        }

        return ret;
    }

    /**
     * remove
     *
     * @param key
     * @return
     */
    public V remove(Object key) {
        return this.remove(key, true);
    }

    /**
     * TimestampEntryValue
     *
     * @param <V>
     */
    private static class TimestampEntryValue<V> {
        @Getter
        @Setter
        private V value;

        @Getter
        @Setter
        private long timestamp;
    }

    /**
     * LruContainerMap
     *
     * @param <K>
     * @param <V>
     */
    private static class LruContainerMap<K, V extends TimestampEntryValue<?>> extends LinkedHashMap<K, V> {

        private static ExecutorService pool = ThreadPoolUtil.getThreadPool("lru-action");

        @Getter
        private ReentrantLock lock = new ReentrantLock();

        @Getter
        @Setter
        private int maxSize;

        private transient Func.Action2<K, V> onEvict;

        public LruContainerMap(int maxSize, Func.Action2<K, V> onEvict) {
            super(16, 0.75f, true);
            this.maxSize = maxSize;
            this.onEvict = onEvict;
        }

        @Override
        public V put(K key, V value) {
            this.lock.lock();
            try {
                return super.put(key, value);
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public V putIfAbsent(K key, V value) {
            this.lock.lock();
            try {
                V result = super.get(key);
                if (result != null) {
                    return result;
                } else {
                    super.put(key, value);
                    return null;
                }
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public V get(Object key) {
            this.lock.lock();
            try {
                return super.get(key);
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public V remove(Object key) {
            this.lock.lock();
            try {
                V ret = super.remove(key);
                if (this.onEvict != null) {
                    pool.execute(() -> this.onEvict.invoke((K) key, ret));
                }

                return ret;
            } finally {
                this.lock.unlock();
            }
        }

        /**
         * removeUnEvict
         *
         * @param key
         * @return
         */
        public V removeUnEvict(Object key) {
            this.lock.lock();
            try {
                return super.remove(key);
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean ret = this.size() > this.maxSize;
            if (this.onEvict != null && ret) {
                pool.execute(() -> this.onEvict.invoke(eldest.getKey(), eldest.getValue()));
            }

            return ret;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}