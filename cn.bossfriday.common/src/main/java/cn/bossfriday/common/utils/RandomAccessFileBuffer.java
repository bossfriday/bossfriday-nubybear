package cn.bossfriday.common.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * RandomAccessFileBuffer
 *
 * @author chenx
 */
@SuppressWarnings("all")
public class RandomAccessFileBuffer implements Closeable {
    /**
     * 缓冲区 内存页 64KB
     */
    private static final int BUF_PAGE_SIZE = 0x10000;
    /**
     * 缓存区 内存页 掩码 0xffff
     */
    private static final int BUF_PAGE_MARK = BUF_PAGE_SIZE - 1;
    /**
     * 缓存区 内存页 移位数
     */
    private static final int NUM_16 = 16;
    /**
     * 文件写入句柄
     */
    private final RandomAccessFile writeHandle;
    /**
     * 文件读取句柄
     */
    private final RandomAccessFile readHandle;
    /**
     * 定义文件读写锁
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * 缓冲区大小
     */
    private final int bufSize;
    /**
     * 缓冲区使用的页的数量
     */
    private int bufPageCount = 0;
    /**
     * 缓存区数据写入位置
     */
    private int wIndex = 0;
    /**
     * 缓冲区数据持久化位置
     */
    private int pIndex = 0;
    /**
     * 缓冲区
     */
    private byte[][] buf = null;
    /**
     * 文件长度
     */
    private long fileLen;
    /**
     * 缓存区是否回环
     */
    private boolean isWinding = false;

    public RandomAccessFileBuffer(File file) throws IOException {
        this.bufSize = BUF_PAGE_SIZE;
        this.writeHandle = new RandomAccessFile(file, "rw");
        this.readHandle = new RandomAccessFile(file, "r");
        this.fileLen = this.writeHandle.length();
    }

    public RandomAccessFileBuffer(File file, final int bufferSize) throws IOException {
        this.bufSize = bufferSize < BUF_PAGE_SIZE ? BUF_PAGE_SIZE : bufferSize;
        this.writeHandle = new RandomAccessFile(file, "rw");
        this.readHandle = new RandomAccessFile(file, "r");
        this.fileLen = this.writeHandle.length();
    }

    public RandomAccessFileBuffer(final String filePath) throws IOException {
        File file = new File(filePath);
        this.bufSize = BUF_PAGE_SIZE;
        this.writeHandle = new RandomAccessFile(file, "rw");
        this.readHandle = new RandomAccessFile(file, "r");
        this.fileLen = this.writeHandle.length();
    }

    public RandomAccessFileBuffer(final String filePath, final int bufferSize) throws IOException {
        File file = new File(filePath);
        this.bufSize = bufferSize < BUF_PAGE_SIZE ? BUF_PAGE_SIZE : bufferSize;
        this.writeHandle = new RandomAccessFile(file, "rw");
        this.readHandle = new RandomAccessFile(file, "r");
        this.fileLen = this.writeHandle.length();
    }

    /**
     * 刷新缓冲区到磁盘
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        this.append(null, 0, 0, true);
    }

    /**
     * 在文件底部追加写入数据
     *
     * @param data
     * @return
     * @throws IOException
     */
    public long append(final byte[] data) throws IOException {
        return this.append(data, 0, data.length, false);
    }

    /**
     * 在文件底部追加写入数据，并刷新缓冲区数据到磁盘
     *
     * @param data
     * @return
     * @throws IOException
     */
    public long appendAndFlush(final byte[] data) throws IOException {
        return this.append(data, 0, data.length, true);
    }

    /**
     * 将数据写入到内存缓冲区
     *
     * @param src
     * @param srcPos
     * @param destPos
     * @param length
     */
    private void writeToBuffer(final byte[] src, int srcPos, final int destPos, int length) {
        // 缓冲区扩容
        while (destPos + length > this.bufPageCount * BUF_PAGE_SIZE
                && this.bufPageCount * BUF_PAGE_SIZE + BUF_PAGE_SIZE <= this.bufSize) {
            this.buf[this.bufPageCount++] = new byte[BUF_PAGE_SIZE];
        }

        // 将数据写入缓冲区
        for (int p = destPos >>> NUM_16, i = destPos & BUF_PAGE_MARK, len; length > 0; srcPos += len, length -= len, p++, i = 0) {
            len = length > BUF_PAGE_SIZE - i ? BUF_PAGE_SIZE - i : length;
            System.arraycopy(src, srcPos, this.buf[p], i, len);
        }
    }

    /**
     * 从缓冲区内读取
     *
     * @param srcPos
     * @param dest
     * @param destPos
     * @param length
     */
    private void readFromBuffer(final int srcPos, final byte[] dest, int destPos, int length) {
        for (int p = (srcPos >>> NUM_16), i = srcPos & BUF_PAGE_MARK, len; length > 0; destPos += len, length -= len, p++, i = 0) {
            len = length > BUF_PAGE_SIZE - i ? BUF_PAGE_SIZE - i : length;
            System.arraycopy(this.buf[p], i, dest, destPos, len);
        }
    }

    /**
     * 在文件底部追加写入数据
     *
     * @param data
     * @param offset
     * @param len
     * @param flush
     * @return
     * @throws IOException
     */
    public long append(final byte[] data, final int offset, int len, final boolean flush) throws IOException {
        boolean isDataIllegal = data != null && (offset >= data.length || offset + len > data.length);
        if (isDataIllegal) {
            throw new IllegalArgumentException("data error dataLength:" + data.length);
        }

        if (data == null) {
            len = 0;
        }

        this.lock.writeLock().lock();
        try {
            // 初始化缓冲区
            if (this.buf == null && data != null && !flush) {
                int k = (this.bufSize & 0xffff) == 0 ? this.bufSize / BUF_PAGE_SIZE : this.bufSize / BUF_PAGE_SIZE + 1;
                this.buf = new byte[k][];
                this.buf[this.bufPageCount++] = new byte[BUF_PAGE_SIZE];
            }
            // 写入位置至缓冲区最后的距离
            int remains = this.bufSize - this.wIndex;
            // 得出写入文件位置
            long pos = this.fileLen + (this.wIndex >= this.pIndex ? this.wIndex - this.pIndex : this.bufSize + this.wIndex - this.pIndex);
            byte[] bytes = null;

            // 写入数据加上缓存区内未持久化数据超过缓存区大小，需要持久化
            boolean isDataCollapsed = this.wIndex < this.pIndex && (flush || this.wIndex + len >= this.pIndex);
            boolean isDataUnCollapsed = this.wIndex > this.pIndex && (flush || len - remains >= this.pIndex);
            boolean isForceFlush = this.wIndex == this.pIndex && (flush && len > 0 || len >= this.bufSize);
            if (isDataCollapsed) {
                // 数据折叠
                bytes = new byte[this.bufSize - this.pIndex + this.wIndex + len];
                this.readFromBuffer(this.pIndex, bytes, 0, this.bufSize - this.pIndex);
                this.readFromBuffer(0, bytes, this.bufSize - this.pIndex, this.wIndex);
                if (len > 0) {
                    System.arraycopy(data, offset, bytes, this.bufSize - this.pIndex + this.wIndex, len);
                }
            } else if (isDataUnCollapsed) {
                // 数据未折叠
                bytes = new byte[this.wIndex - this.pIndex + len];
                this.readFromBuffer(this.pIndex, bytes, 0, this.wIndex - this.pIndex);
                if (len > 0) {
                    System.arraycopy(data, offset, bytes, this.wIndex - this.pIndex, len);
                }
            } else if (isForceFlush) {
                // 强制持久化
                bytes = new byte[len];
                System.arraycopy(data, offset, bytes, 0, len);
            }

            // 数据加入缓冲区
            if (this.buf != null) {
                if (len > remains) {
                    this.writeToBuffer(data, offset, this.wIndex, remains);
                    int l = remains;

                    while (len - l > this.bufSize) {
                        this.writeToBuffer(data, offset + l, 0, this.bufSize);
                        l += this.bufSize;
                    }

                    this.writeToBuffer(data, offset + l, 0, len - l);
                    this.wIndex = len - l;
                    this.isWinding = true;
                } else if (len > 0) {
                    this.writeToBuffer(data, offset, this.wIndex, len);
                    this.wIndex += len;
                    if (this.wIndex == this.bufSize) {
                        this.wIndex = 0;
                    }
                }
            }
            // 数据写入文件
            if (bytes != null && bytes.length > 0) {
                this.writeHandle.seek(this.fileLen);
                this.writeHandle.write(bytes);
                this.fileLen += bytes.length;
                this.pIndex = this.wIndex;
            }

            return pos;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * 从文件的任意位置读取数据
     *
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] read(long offset, int length) throws IOException {
        this.lock.readLock().lock();
        try {
            // 计算文件数据的实际长度
            long rFileLen = this.fileLen + (this.wIndex >= this.pIndex ? this.wIndex - this.pIndex : this.bufSize + this.wIndex - this.pIndex);

            if (offset > rFileLen) {
                return new byte[0];
            }
            if (offset + length > rFileLen) {
                length = (int) (rFileLen - offset);
            }

            byte[] data = new byte[length];
            // 获取已使用的buf空间大小
            int userBufSize = this.isWinding ? this.bufSize : this.wIndex;
            int destPos = 0;

            // offset 不在缓存区内，直接读取文件里面的内容
            if (offset < rFileLen - userBufSize) {
                synchronized (this) {
                    this.readHandle.seek(offset);
                    destPos = this.readHandle.read(data);
                }
                if (destPos == length) {
                    return data;
                }
                offset += destPos;
                length -= destPos;
            }

            // offset 读取的数据在缓存区内
            if (this.buf != null && offset >= rFileLen - userBufSize) {
                // 计算offset在缓存区中的相对位置
                offset = this.wIndex + offset + this.bufSize - rFileLen;
                // 缓冲区折叠
                if (offset < this.bufSize && offset + length > this.bufSize) {
                    int remains = (int) (this.bufSize - offset);
                    this.readFromBuffer((int) offset, data, destPos, remains);
                    this.readFromBuffer(0, data, destPos + remains, length - remains);
                } else {
                    if (offset >= this.bufSize) {
                        offset -= this.bufSize;
                    }

                    this.readFromBuffer((int) offset, data, destPos, length);
                }
            }

            return data;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * 在文件的任意位置写入数据
     *
     * @param offset
     * @param data
     * @throws IOException
     */
    public void write(long offset, final byte[] data) throws IOException {
        this.lock.writeLock().lock();

        try {
            long pos = this.fileLen + (this.wIndex >= this.pIndex ? this.wIndex - this.pIndex : this.bufSize + this.wIndex - this.pIndex);
            int length = data.length;

            int userBufSize = this.isWinding ? this.bufSize : this.wIndex;
            if (this.buf != null && offset >= pos - userBufSize && offset + length <= pos) {
                offset = this.wIndex + offset + this.bufSize - pos;

                if (offset < this.bufSize && offset + length > this.bufSize) {
                    int remains = (int) (this.bufSize - offset);
                    this.writeToBuffer(data, 0, (int) offset, remains);
                    this.writeToBuffer(data, length - remains, 0, length - remains);
                } else {
                    if (offset >= this.bufSize) {
                        offset -= this.bufSize;
                    }
                    this.writeToBuffer(data, 0, (int) offset, length);
                }
            } else if (offset < this.fileLen) {
                this.writeHandle.seek(offset);
                this.writeHandle.write(data);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * 释放缓冲区
     *
     * @throws IOException
     */
    public void freeBuffer() throws IOException {
        this.lock.writeLock().lock();
        try {
            if (this.buf != null) {
                this.flush();
                this.buf = null;
                this.wIndex = this.pIndex = 0;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public final void close() throws IOException {
        this.freeBuffer();
        this.writeHandle.close();
        this.readHandle.close();
    }

    /**
     * 获取文件长度
     *
     * @return
     */
    public long length() {
        this.lock.readLock().lock();
        try {
            return this.fileLen + (this.wIndex >= this.pIndex ? this.wIndex - this.pIndex : this.bufSize + this.wIndex - this.pIndex);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
