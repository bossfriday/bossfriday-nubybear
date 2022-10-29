package cn.bossfriday.common.test.file;

import cn.bossfriday.common.utils.FileUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileTest2 {
    public static void main(String[] args) throws Exception {
        File srcFile1 = new File("D:\\tmp\\1.mp4");
        int chunkSize = 20 * 1024 * 1024;
        int fileTotalSize = (int) srcFile1.length();
        int chunkCount = fileTotalSize % chunkSize == 0 ? (int) (fileTotalSize / chunkSize) : (int) (fileTotalSize / chunkSize + 1);

        File destFile = new File("D:\\tmp\\1.data");
        if (!destFile.exists())
            destFile.createNewFile();

        FileChannel destFileChannel = new RandomAccessFile(destFile, "rw").getChannel();
        long begin = System.currentTimeMillis();
        for (int i = 0; i < chunkCount; i++) {
            int beginOffset = i * chunkSize;
            int endOffset = (i + 1) * chunkSize - 1;
            if (endOffset > fileTotalSize) {
                endOffset = (int) fileTotalSize - 1;
            }

            int partLength = endOffset - beginOffset + 1;
            byte[] partData = new byte[partLength];
            read(srcFile1, beginOffset, partData);

            FileUtil.transferFrom(destFileChannel, partData, beginOffset);
        }

        System.out.println("------done:" + (System.currentTimeMillis() - begin));
    }

    public static void read(File file, long pos, byte[] tgtBytes) throws Exception {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(pos);
            raf.readFully(tgtBytes);
        } finally {
            raf.close();
        }
    }
}
