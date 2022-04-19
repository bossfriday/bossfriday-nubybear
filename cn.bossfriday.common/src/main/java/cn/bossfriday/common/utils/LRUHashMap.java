package cn.bossfriday.common.utils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class LRUHashMap<K, V> {

    /**
     * 线程池
     */
    private static ScheduledExecutorService expireExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("lru-schedule-pool-%d")
                    .setDaemon(true)
                    .build()
    );

    /**
     * 是否清除中, 避免同时多次调用
     */
    private AtomicBoolean isCleanerRunning = new AtomicBoolean(false);

    /**
     * LRU-MAP容器
     */
    private LRUContainerMap<K, TimestampEntryValue<V>> container;

    /**
     * 用于清理过期键的线程
     */
    private Runnable expireRunnable = new Runnable() {

        @Override
        public void run() {
            long nextInterval = 1000;
            container.lock();
            try {
                // 开启'停止清理'的标记
                boolean shouldStopCleaner = true;

                //如果容器内有东西
                if (container.size() > 0) {
                    long now = System.currentTimeMillis();
                    // 创建一个队列,用于存放要移除的内容
                    List<K> toBeRemoved = new ArrayList<>();
                    for (Entry<K, TimestampEntryValue<V>> e : container.entrySet()) {
                        K key = e.getKey();
                        TimestampEntryValue<V> tValue = e.getValue();
                        // 当前时间减去出生时间是否大于生存时间
                        long timeLapsed = now - tValue.timestamp;

                        // 如果过期, 那么将相应的key放入到队列中
                        if (timeLapsed >= duration) {
                            toBeRemoved.add(key);
                        } else {
                            //设置下一次删除的时间(周期)
                            long delta = duration - timeLapsed;
                            if (delta > 1000L) {
                                nextInterval = delta;
                            }
                            break;
                        }
                    }

                    // 如果待删除队列里有东西, 那就一个一个remove
                    if (toBeRemoved.size() > 0) {
                        for (K key : toBeRemoved) {
                            container.remove(key);
                        }
                    }

                    //如果container在经历删除后还是有东西, 那么就不该停止清理
                    if (container.size() > 0) {
                        shouldStopCleaner = false;
                    }
                }

                //是否该停止清理, 正常的话应该是该停止
                if (shouldStopCleaner) {
                    // put操作会让isCleanerRunning变为true
                    isCleanerRunning.compareAndSet(true, false);

                    // 如果不认为该停止, 那就再调用一遍任务
                } else {
                    expireExecutor.schedule(this, nextInterval, TimeUnit.MILLISECONDS);
                }

            } finally {
                container.unlock();
            }
        }
    };

    /**
     * 每个key应该存活的时间
     */
    private long duration = -1;

    public LRUHashMap(int maxSize, final F.Action2<K, V> onEvict) {
        this(maxSize, onEvict, -1L);
    }

    public LRUHashMap(int maxSize, final F.Action2<K, V> onEvict, long duration) {
        F.Action2<K, TimestampEntryValue<V>> doOnEvict = null;

        if (onEvict != null) {
            doOnEvict = (key, value) -> {
                if (value != null) {
                    onEvict.invoke(key, value.value);
                }
            };
        }
        this.duration = duration;
        container = new LRUContainerMap(maxSize, doOnEvict);
    }

    public int size() {
        return container.size();
    }

    int getMaxSize() {
        return container.getMaxSize();
    }

    public Set<K> getKeys() {
        return container.keySet();
    }

    void setMaxSize(int maxSize) {
        container.setMaxSize(maxSize);
    }

    public long getDuration() {
        return duration;
    }

    public V put(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = container.put(key, v);

        if (duration > 0) {
            if (isCleanerRunning.compareAndSet(false, true)) {
                expireExecutor.schedule(expireRunnable, duration, TimeUnit.MILLISECONDS);
            }
        }

        return old == null ? null : old.value;
    }

    public V putIfAbsent(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = container.putIfAbsent(key, v);

        if (old == null) {
            if (duration > 0) {
                if (isCleanerRunning.compareAndSet(false, true)) {
                    expireExecutor.schedule(expireRunnable, duration, TimeUnit.MILLISECONDS);
                }
            }
        }

        return old == null ? null : old.value;
    }

    public boolean containsKey(Object key) {
        return container.containsKey(key);
    }

    public V get(Object key) {
        TimestampEntryValue<V> got = container.get(key);
        V ret = null;
        if (got != null) {
            got.timestamp = System.currentTimeMillis();
            ret = got.value;
        }
        return ret;
    }

    public V remove(Object key, boolean doEvict) {
        TimestampEntryValue<V> removed;
        if (doEvict) {
            removed = container.remove(key);
        } else {
            removed = container.removeUnEvict(key);
        }

        V ret = null;
        if (removed != null) {
            ret = removed.value;
        }
        return ret;
    }

    public V remove(Object key) {
        return remove(key, true);
    }

    @Override
    public Map<K, Object> clone() {
        return container.clone();
    }

    static class TimestampEntryValue<V> {
        public V value;
        public long timestamp;
    }

    /**
     * 容器对象定义
     * 继承自{@link LinkedHashMap}
     *
     * @param <K>
     * @param <V> 带时间戳的value对象
     */
    private static class LRUContainerMap<K, V extends TimestampEntryValue<?>> extends LinkedHashMap<K, V> {

        /**
         * 线程池, 用于执行F.Action2 onEvict
         */
        //        private static ExecutorService pool = ThreadPoolHelper.getCommonThreadPool();
        private static ExecutorService pool = ThreadPoolUtil.getThreadPool("lru-action");
        private static final long serialVersionUID = -2108033306317724707L;

        /**
         * 重入锁, 保证线程安全
         */
        private ReentrantLock lock = new ReentrantLock();

        /**
         * 容器最大数量
         * <p>
         * LinkedHashMap在插入操作之后会调用 afterNodeInsertion(boolean evict),
         * 而这个 方法会根据removeEldestEntry的返回值来判断
         * 是否要删除最老的元素
         *
         * @implNote 简单说就是达到最大值后就会删除最老的元素
         */
        private int maxSize;

        private F.Action2<K, V> onEvict;

        public LRUContainerMap(int maxSize, F.Action2<K, V> onEvict) {
            super(16, 0.75f, true);
            this.maxSize = maxSize;
            this.onEvict = onEvict;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        @Override
        public V put(K key, V value) {
            lock();
            try {
                return super.put(key, value);
            } finally {
                unlock();
            }
        }

        @Override
        public V putIfAbsent(K key, V value) {
            lock();
            try {
                V result = super.get(key);
                if (result != null) {
                    return result;
                } else {
                    super.put(key, value);
                    return null;
                }
            } finally {
                unlock();
            }
        }

        @Override
        public V get(Object key) {
            lock.lock();
            try {
                return super.get(key);
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public V remove(final Object key) {
            lock();
            try {
                final V ret = super.remove(key);
                if (onEvict != null) {
                    pool.execute(() -> {
                        try {
                            onEvict.invoke((K) key, ret);
                        } catch (Exception ignore) {
                        }
                    });
                }
                return ret;
            } finally {
                unlock();
            }
        }

        public V removeUnEvict(final Object key) {
            lock();
            try {
                final V ret = super.remove(key);

                return ret;
            } finally {
                unlock();
            }
        }

        /**
         * LinkedHashMap在插入操作之后会调用 afterNodeInsertion(boolean evict),
         * 而这个 方法会根据本方法的返回值来判断
         * 是否要删除最老的元素
         *
         * @implNote 简单说就是达到最大值后就会删除最老的元素
         */
        @Override
        protected boolean removeEldestEntry(final Entry<K, V> eldest) {
            final boolean ret = size() > maxSize;
            if (onEvict != null && ret) {
                pool.execute(() -> onEvict.invoke(eldest.getKey(), eldest.getValue()));
            }
            return ret;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<K, Object> clone() {
            Map<K, V> map;

            lock();
            try {
                map = (Map<K, V>) super.clone();
            } finally {
                unlock();
            }

            Iterator<Entry<K, V>> iter = map.entrySet()
                    .iterator();
            Map<K, Object> result = new HashMap<>(map.entrySet().size() * 2);
            while (iter.hasNext()) {
                Entry<K, V> entry = iter.next();
                result.put(entry.getKey(), entry.getValue().value);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        LRUHashMap<String, String> map = new LRUHashMap<>(10, new F.Action2<String, String>() {

            @Override
            public void invoke(String key, String value) {
                System.out.println("Entry evicted: key = " + key + ", value = " + value);
            }
        }, 5000);

        for (int i = 0; i < 11; i++) {
            map.put("key" + i, "value" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}