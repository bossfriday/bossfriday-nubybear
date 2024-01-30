package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * FileUtil
 *
 * @author chenx
 */
public class FileUtil {

    private FileUtil() {

    }

    /**
     * create
     *
     * @param file
     * @param length
     * @throws Exception
     */
    public static void create(File file, long length) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(length);
        }
    }

    /**
     * getFileExt
     *
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }

        return fileName.substring(index + 1).toLowerCase();
    }

    /**
     * transferFrom（零拷贝写入数据）
     *
     * @param destFileChannel
     * @param data
     * @param position
     * @param isCloseDestFileChannel
     * @throws Exception
     */
    public static void transferFrom(FileChannel destFileChannel, byte[] data, long position, boolean isCloseDestFileChannel) throws IOException {
        if (destFileChannel == null) {
            throw new ServiceRuntimeException("destFileChannel is null!");
        }

        try (ByteArrayInputStream srcInput = new ByteArrayInputStream(data);
             ReadableByteChannel srcChannel = Channels.newChannel(srcInput);) {
            destFileChannel.transferFrom(srcChannel, position, data.length);
        } finally {
            if (isCloseDestFileChannel) {
                destFileChannel.close();
            }
        }
    }

    /**
     * transferFrom
     *
     * @param destFileChannel
     * @param data
     * @param position
     * @throws IOException
     */
    public static void transferFrom(FileChannel destFileChannel, byte[] data, long position) throws IOException {
        transferFrom(destFileChannel, data, position, false);
    }

    /**
     * transferTo(零拷贝读取数据)
     *
     * @param srcFileChannel
     * @param position
     * @param length
     * @param isCloseSrcFileChannel
     * @return
     * @throws IOException
     */
    public static byte[] transferTo(FileChannel srcFileChannel, long position, long length, boolean isCloseSrcFileChannel) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             WritableByteChannel destChannel = Channels.newChannel(out);) {
            srcFileChannel.transferTo(position, length, destChannel);
            return out.toByteArray();
        } finally {
            if (isCloseSrcFileChannel) {
                srcFileChannel.close();
            }
        }
    }

    /**
     * transferTo
     *
     * @param srcFileChannel
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public static byte[] transferTo(FileChannel srcFileChannel, long position, int length) throws IOException {
        return transferTo(srcFileChannel, position, length, true);
    }

    /**
     * readFile
     *
     * @param file
     * @param offset
     * @param targetBytes
     * @throws Exception
     */
    public static void readFile(File file, long offset, byte[] targetBytes) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            raf.readFully(targetBytes);
        }
    }
}
