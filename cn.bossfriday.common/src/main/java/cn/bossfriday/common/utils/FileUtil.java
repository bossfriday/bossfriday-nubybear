package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.BizException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

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
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
     * 目标文件零拷贝写入数据
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
}
