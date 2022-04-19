package cn.bossfriday.common.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RandomAccessFileBuffer implements Closeable {
    /** 缓冲区 内存页 64KB */
    private static final int BUF_PAGE_SIZE = 0x10000;
    /** 缓存区 内存页 掩码 0xffff */
    private static final int BUF_PAGE_MARK = BUF_PAGE_SIZE - 1;
    /** 缓存区 内存页 移位数 */
    private static final int NUM_16 = 16;
    /** 文件写入句柄 */
    private final RandomAccessFile writeHandle;
    /** 文件读取句柄 */
    private final RandomAccessFile readHandle;
    /** 定义文件读写锁 */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /** 缓冲区大小 */
    private final int bufSize;
    /** 缓冲区使用的页的数量 */
    private int bufPageCount = 0;
    /** 缓存区数据写入位置 */
    private int wIndex = 0;
    /** 缓冲区数据持久化位置 */
    private int pIndex = 0;
    /** 缓冲区 */
    private byte[][] buf = null;
    /** 文件长度 */
    private long fileLen;
    /** 缓存区是否回环 */
    private boolean isWinding = false;

    public RandomAccessFileBuffer(File file) throws IOException {
        bufSize = BUF_PAGE_SIZE;
        writeHandle = new RandomAccessFile(file, "rw");
        readHandle = new RandomAccessFile(file, "r");
        fileLen = writeHandle.length();
    }

    public RandomAccessFileBuffer(File file, final int bufferSize) throws IOException {
        bufSize = bufferSize < BUF_PAGE_SIZE ? BUF_PAGE_SIZE : bufferSize;
        writeHandle = new RandomAccessFile(file, "rw");
        readHandle = new RandomAccessFile(file, "r");
        fileLen = writeHandle.length();
    }

    public RandomAccessFileBuffer(final String filePath) throws IOException {
        File file = new File(filePath);
        bufSize = BUF_PAGE_SIZE;
        writeHandle = new RandomAccessFile(file, "rw");
        readHandle = new RandomAccessFile(file, "r");
        fileLen = writeHandle.length();
    }

    public RandomAccessFileBuffer(final String filePath, final int bufferSize) throws IOException {
        File file = new File(filePath);
        bufSize = bufferSize < BUF_PAGE_SIZE ? BUF_PAGE_SIZE : bufferSize;
        writeHandle = new RandomAccessFile(file, "rw");
        readHandle = new RandomAccessFile(file, "r");
        fileLen = writeHandle.length();
    }

    /**
     * 刷新缓冲区到磁盘
     */
    public void flush() throws IOException {
        append(null, 0, 0, true);
    }

    /**
     * 在文件底部追加写入数据
     */
    public long append(final byte[] data) throws IOException {
        return append(data, 0, data.length, false);
    }

    /**
     * 在文件底部追加写入数据，并刷新缓冲区数据到磁盘
     */
    public long appendAndFlush(final byte[] data) throws IOException {
        return append(data, 0, data.length, true);
    }

    /**
     * 将数据写入到内存缓冲区
     */
    private void writeToBuffer(final byte[] src, int srcPos, final int destPos, int length) {
        // 缓冲区扩容
        while (destPos + length > bufPageCount * BUF_PAGE_SIZE
                && bufPageCount * BUF_PAGE_SIZE + BUF_PAGE_SIZE <= bufSize) {
            buf[bufPageCount++] = new byte[BUF_PAGE_SIZE];
        }

        // 将数据写入缓冲区
        for (int p = destPos >>> NUM_16, i = destPos
                & BUF_PAGE_MARK, len; length > 0; srcPos += len, length -= len, p++, i = 0) {
            len = length > BUF_PAGE_SIZE - i ? BUF_PAGE_SIZE - i : length;
            System.arraycopy(src, srcPos, buf[p], i, len);
        }
    }

    /***
     * 从缓冲区内读取
     */
    private void readFromBuffer(final int srcPos, final byte[] dest, int destPos, int length) {
        for (int p = (srcPos >>> NUM_16), i = srcPos
                & BUF_PAGE_MARK, len; length > 0; destPos += len, length -= len, p++, i = 0) {
            len = length > BUF_PAGE_SIZE - i ? BUF_PAGE_SIZE - i : length;
            System.arraycopy(buf[p], i, dest, destPos, len);
        }
    }

    /**
     * 在文件底部追加写入数据
     */
    public long append(final byte[] data, final int offset, int len, final boolean flush) throws IOException {
        if (data != null && (offset >= data.length || offset + len > data.length)) {
            throw new IllegalArgumentException("data error dataLength:" + data.length);
        }

        if (data == null) {
            len = 0;
        }

        lock.writeLock().lock();
        try {
            // 初始化缓冲区
            if (buf == null && data != null && !flush) {
                int k = (bufSize & 0xffff) == 0 ? bufSize / BUF_PAGE_SIZE : bufSize / BUF_PAGE_SIZE + 1;
                buf = new byte[k][];
                buf[bufPageCount++] = new byte[BUF_PAGE_SIZE];
            }
            // 写入位置至缓冲区最后的距离
            int remains = bufSize - wIndex;
            // 得出写入文件位置
            long pos = fileLen + (wIndex >= pIndex ? wIndex - pIndex : bufSize + wIndex - pIndex);
            byte[] bytes = null;

            // 写入数据加上缓存区内未持久化数据超过缓存区大小，需要持久化
            // 数据折叠
            if (wIndex < pIndex && (flush || wIndex + len >= pIndex)) {
                bytes = new byte[bufSize - pIndex + wIndex + len];
                readFromBuffer(pIndex, bytes, 0, bufSize - pIndex);
                readFromBuffer(0, bytes, bufSize - pIndex, wIndex);
                if (len > 0) {
                    System.arraycopy(data, offset, bytes, bufSize - pIndex + wIndex, len);
                }
            } else
            // 数据未折叠
            if (wIndex > pIndex && (flush || len - remains >= pIndex)) {
                bytes = new byte[wIndex - pIndex + len];
                readFromBuffer(pIndex, bytes, 0, wIndex - pIndex);
                if (len > 0) {
                    System.arraycopy(data, offset, bytes, wIndex - pIndex, len);
                }
            } else
            // 强制持久化
            if (wIndex == pIndex && (flush && len > 0 || len >= bufSize)) {
                bytes = new byte[len];
                System.arraycopy(data, offset, bytes, 0, len);
            }

            // 数据加入缓冲区
            if (buf != null) {
                if (len > remains) {
                    writeToBuffer(data, offset, wIndex, remains);
                    int l = remains;

                    while (len - l > bufSize) {
                        writeToBuffer(data, offset + l, 0, bufSize);
                        l += bufSize;
                    }

                    writeToBuffer(data, offset + l, 0, len - l);
                    wIndex = len - l;
                    isWinding = true;
                } else if (len > 0) {
                    writeToBuffer(data, offset, wIndex, len);
                    wIndex += len;
                    if (wIndex == bufSize) {
                        wIndex = 0;
                    }
                }
            }
            // 数据写入文件
            if (bytes != null && bytes.length > 0) {
                writeHandle.seek(fileLen);
                writeHandle.write(bytes);
                fileLen += bytes.length;
                pIndex = wIndex;
            }

            return pos;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 从文件的任意位置读取数据
     */
    public byte[] read(long offset, int length) throws IOException {
        lock.readLock().lock();
        try {
            // 计算文件数据的实际长度
            long rFLen = fileLen + (wIndex >= pIndex ? wIndex - pIndex : bufSize + wIndex - pIndex);

            if (offset > rFLen) {
                return null;
            }
            if (offset + length > rFLen) {
                length = (int) (rFLen - offset);
            }

            byte[] data = new byte[length];
            // 获取已使用的buf空间大小
            int userBufSize = isWinding ? bufSize : wIndex;
            int destPos = 0;

            // offset 不在缓存区内，直接读取文件里面的内容
            if (offset < rFLen - userBufSize) {
                synchronized (this) {
                    readHandle.seek(offset);
                    destPos = readHandle.read(data);
                }
                if (destPos == length) {
                    return data;
                }
                offset += destPos;
                length -= destPos;
            }

            // offset 读取的数据在缓存区内
            if (buf != null && offset >= rFLen - userBufSize) {
                // 计算offset在缓存区中的相对位置
                offset = wIndex + offset + bufSize - rFLen;
                // 缓冲区折叠
                if (offset < bufSize && offset + length > bufSize) {
                    int remains = (int) (bufSize - offset);
                    readFromBuffer((int) offset, data, destPos, remains);
                    readFromBuffer(0, data, destPos + remains, length - remains);
                } else {
                    if (offset >= bufSize) {
                        offset -= bufSize;
                    }

                    readFromBuffer((int) offset, data, destPos, length);
                }
            }

            return data;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 在文件的任意位置写入数据
     */
    public void write(long offset, final byte[] data) throws IOException {
        lock.writeLock().lock();

        try {
            long pos = fileLen + (wIndex >= pIndex ? wIndex - pIndex : bufSize + wIndex - pIndex);
            int length = data.length;

            int userBufSize = isWinding ? bufSize : wIndex;
            if (buf != null && offset >= pos - userBufSize && offset + length <= pos) {
                offset = wIndex + offset + bufSize - pos;

                if (offset < bufSize && offset + length > bufSize) {
                    int remains = (int) (bufSize - offset);
                    writeToBuffer(data, 0, (int) offset, remains);
                    writeToBuffer(data, length - remains, 0, length - remains);
                } else {
                    if (offset >= bufSize) {
                        offset -= bufSize;
                    }
                    writeToBuffer(data, 0, (int) offset, length);
                }
            } else if (offset < fileLen) {
                writeHandle.seek(offset);
                writeHandle.write(data);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 释放缓冲区
     */
    public void freeBuffer() throws IOException {
        lock.writeLock().lock();
        try {
            if (buf != null) {
                flush();
                buf = null;
                wIndex = pIndex = 0;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public final void close() throws IOException {
        freeBuffer();
        writeHandle.close();
        readHandle.close();
    }

    /**
     * 获取文件长度
     *
     * @return 文件长度
     */
    public long length() {
        lock.readLock().lock();
        try {
            return fileLen + (wIndex >= pIndex ? wIndex - pIndex : bufSize + wIndex - pIndex);
        } finally {
            lock.readLock().unlock();
        }
    }
}
