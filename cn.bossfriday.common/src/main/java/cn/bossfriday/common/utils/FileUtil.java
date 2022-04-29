package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.BizException;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileUtil {
    /**
     * 创建制定大小的文件
     */
    public static void create(File file, long length) throws Exception {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.setLength(length);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    /**
     * 获取文件扩展名
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
     */
    public static void transferFrom(FileChannel destFileChannel, byte[] data, long position, boolean isCloseDestFileChannel) throws Exception {
        if (destFileChannel == null)
            throw new BizException("destFileChannel is null!");

        ByteArrayInputStream srcInput = null;
        ReadableByteChannel srcChannel = null;
        try {
            srcInput = new ByteArrayInputStream(data);
            srcChannel = Channels.newChannel(srcInput);
            destFileChannel.transferFrom(srcChannel, position, data.length);
        } finally {
            if (srcInput != null)
                srcInput.close();

            if (srcChannel != null)
                srcChannel.close();

            if (isCloseDestFileChannel)
                destFileChannel.close();
        }
    }

    public static void transferFrom(FileChannel destFileChannel, byte[] data, long position) throws Exception {
        transferFrom(destFileChannel, data, position, false);
    }

    /**
     * 零拷贝读取数据
     */
    public static byte[] transferTo(FileChannel srcFileChannel, long position, long length, boolean isCloseSrcFileChannel) throws Exception {
        WritableByteChannel destChannel = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            destChannel = Channels.newChannel(out);

            srcFileChannel.transferTo(position, length, destChannel);
            return out.toByteArray();
        } finally {
            if (out != null)
                out.close();

            if (destChannel != null)
                destChannel.close();

            if (isCloseSrcFileChannel)
                srcFileChannel.close();
        }
    }

    public static byte[] transferTo(FileChannel srcFileChannel, long position, int length) throws Exception {
        return transferTo(srcFileChannel, position, length, true);
    }
}
