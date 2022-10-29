package cn.bossfriday.common.test.file;

import cn.bossfriday.common.utils.RandomAccessFileBuffer;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * https://stackoverflow.com/questions/30018090/filechannel-zero-copy-transferto-fails-to-copy-bytes-to-socketchannel
 * <p>
 * https://stackoverflow.com/questions/71123547/zero-copy-netty-bytebuf-into-a-file-via-filechannel
 */
public class FileTest {
    public static void main(String[] args) throws Exception {
        File destFile = new File("D:\\tmp\\1.data");
        File srcFile1 = new File("D:\\tmp\\1.pdf");
        File srcFile2 = new File("D:\\tmp\\2.pdf");

//        if(!destFile.exists())
//            create(destFile, 100 * 1024 * 1024);

        if (!destFile.exists())
            destFile.createNewFile();

        long position = 0;
        long srcFileSize = srcFile1.length();
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            copyFileWithFileChannel2(srcFile1, destFile, position);
            position += srcFileSize;
        }
        long time = System.currentTimeMillis() - begin;
        System.out.println(time);


        // 写文件验证
//        byte[] data1 = new byte[5769364];
//        byte[] data2 = new byte[1187719];
//        read(srcFile1,0,data1);
//        read(srcFile2,0,data2);
//
//        write("D:\\tmp\\1.data", 0, data1);
//        write("D:\\tmp\\1.data", 5769364, data2);

//        copyFileWithFileChannel2(srcFile1, destFile, 0);
//        copyFileWithFileChannel2(srcFile2, destFile, 5769364);


        // 读文件验证
//        byte[] data3 = new byte[5769364];
//        byte[] data4 = new byte[1187719];
//        read(destFile,0,data3);
//        read(destFile,5769364,data4);

//        RandomAccessFileBuffer fileBuffer = new RandomAccessFileBuffer("D:\\tmp\\1.data" );
//        byte[] data3 = fileBuffer.read(0,5769364);
//        byte[] data4 = fileBuffer.read(5769364,1187719);
//
//        bytesToFile(data3,"D:\\tmp\\1-read.pdf");
//        bytesToFile(data4,"D:\\tmp\\2-read.pdf");
    }

    public static void copyFileWithFileChannel(File srcFile, File destFile, long position) throws Exception {
        FileChannel srcFileChannel = null;
        FileChannel destFileChannel = null;
        System.out.println(position);
        try {
            if (!srcFile.exists())
                throw new Exception("Source file not existed!");

            if (!destFile.exists())
                throw new Exception("Destination file not existed!");

            srcFileChannel = new RandomAccessFile(srcFile, "r").getChannel();
            destFileChannel = new RandomAccessFile(destFile, "rw").getChannel();
            srcFileChannel.transferTo(position, srcFileChannel.size(), destFileChannel);
        } finally {
            srcFileChannel.close();
            destFileChannel.close();
        }
    }

    public static void copyFileWithFileChannel2(File srcFile, File destFile, long position) throws Exception {
        FileChannel srcFileChannel = null;
        FileChannel destFileChannel = null;
//        System.out.println(position);
        try {
            if (!srcFile.exists())
                throw new Exception("Source file not existed!");

            if (!destFile.exists())
                throw new Exception("Destination file not existed!");

            srcFileChannel = new RandomAccessFile(srcFile, "r").getChannel();
            destFileChannel = new RandomAccessFile(destFile, "rw").getChannel();
            destFileChannel.transferFrom(srcFileChannel, position, srcFileChannel.size());
        } finally {
            srcFileChannel.close();
            //destFileChannel.close();
        }
    }

    public static void copyFileWithFileChannel3(byte[] data, File destFile, long position) throws Exception {
        FileChannel destFileChannel = null;
        ByteArrayInputStream srcInput = null;
        ReadableByteChannel srcChannel = null;
        try {
            srcChannel = Channels.newChannel(srcInput);
            destFileChannel = new RandomAccessFile(destFile, "rw").getChannel();
            destFileChannel.transferFrom(srcChannel, position, data.length);
        } finally {
            if (srcInput != null)
                srcInput.close();

            if (destFileChannel != null)
                destFileChannel.close();
        }
    }

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
                    // ignore
                }
            }
        }
    }

    public static void read(File file, long pos, byte[] tgtBytes) throws Exception {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(pos);
            raf.readFully(tgtBytes);
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static void write(String filePath, long pos, byte[] data) throws Exception {
        RandomAccessFile raf = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception("File Not Existed!(" + filePath + ")");
            }

            raf = new RandomAccessFile(file, "rw");
            raf.seek(pos);
            raf.write(data);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    public static void bytesToFile(byte[] buffer, final String filePath) {
        File file = new File(filePath);
        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;
        try {
            output = new FileOutputStream(file);
            bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != bufferedOutput) {
                try {
                    bufferedOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
