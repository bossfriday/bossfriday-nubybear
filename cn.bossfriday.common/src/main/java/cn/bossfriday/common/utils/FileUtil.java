package cn.bossfriday.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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
}
