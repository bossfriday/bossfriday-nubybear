package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.LRUHashMap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.core.StorageEngineVersion;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static cn.bossfriday.fileserver.common.FileServerConst.FILE_DEFAULT_EXT;
import static cn.bossfriday.fileserver.common.FileServerConst.FILE_UPLOADING_TMP_FILE_EXT;

@Slf4j
@StorageEngineVersion
public class TmpFileHandler implements ITmpFileHandler {
    private LRUHashMap<String, FileChannel> tmpFileChannelMap = new LRUHashMap<>(10000, null, 1000 * 60 * 60L * 2);

    @Override
    public WriteTmpFileResult write(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        FileChannel tmpFileAccess = null;
        WriteTmpFileResult result = null;

        try {
            FileTransactionContext ctx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
            if (ctx == null)
                throw new Exception("FileTransactionContext is null!(" + fileTransactionId + ")");

            tmpFileAccess = getTmpFileChannel(msg);
            FileUtil.transferFrom(tmpFileAccess, msg.getData(), msg.getOffset());
            ctx.addTmpFileSaveSize(msg.getData().length);

            // 临时文件完成
            if (ctx.isCloseTmpFileAccess()) {
                tmpFileAccess.close();
                tmpFileChannelMap.remove(fileTransactionId);

                String fileExtName = renameTmpFile(msg);
                result = new WriteTmpFileResult();
                result.setFileTransactionId(msg.getFileTransactionId());
                result.setResult(OperationResult.OK);
                result.setStorageEngineVersion(msg.getStorageEngineVersion());
                result.setNamespace(msg.getNamespace());
                result.setClusterNodeName(FileServerConfigManager.getCurrentClusterNodeName());
                result.setKeepAlive(msg.isKeepAlive());
                result.setTimestamp(System.currentTimeMillis());
                result.setFileTotalSize(msg.getFileTotalSize());
                result.setFileName(msg.getFileName());
                result.setFileExtName(fileExtName);
                log.info("tmpFile process done :" + fileTransactionId);
            }
        } catch (Exception ex) {
            log.error("write tmpFile error!", ex);
            if (tmpFileAccess != null) {
                tmpFileAccess.close();
                tmpFileChannelMap.remove(fileTransactionId);
            }

            result = new WriteTmpFileResult(fileTransactionId, OperationResult.SystemError);
        }

        return result;
    }

    private synchronized FileChannel getTmpFileChannel(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        if (tmpFileChannelMap.containsKey(fileTransactionId))
            return tmpFileChannelMap.get(fileTransactionId);

        File tmpFile = getTmpFile(fileTransactionId);
        if (!tmpFile.exists())
            FileUtil.create(tmpFile, msg.getFileSize());

        FileChannel destFileChannel = new RandomAccessFile(tmpFile, "rw").getChannel();
        tmpFileChannelMap.put(fileTransactionId, destFileChannel);

        return destFileChannel;
    }

    /**
     * getTmpFile
     */
    private static File getTmpFile(String fileTransactionId) {
        File tmpDir = StorageEngine.getInstance().getTmpDir();
        String tmpFileName = fileTransactionId + "." + FILE_UPLOADING_TMP_FILE_EXT;

        return new File(tmpDir, tmpFileName);
    }

    /**
     * renameTmpFile
     *
     * @param msg
     * @return rename后的临时文件扩展名
     */
    private static String renameTmpFile(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        File tmpFile = getTmpFile(fileTransactionId);
        String extName = FileUtil.getFileExt(msg.getFileName()).toLowerCase();
        if (StringUtils.isEmpty(extName))
            extName = FILE_DEFAULT_EXT;

        String newFilePath = tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().lastIndexOf(".")) + "." + extName;
        File newFile = new File(newFilePath);
        if (newFile.exists()) {
            log.warn("newTmpFile already existed: " + fileTransactionId);
            return extName;
        }

        if (!tmpFile.renameTo(newFile)) {
            throw new Exception("rename tmpFile failed: " + fileTransactionId);
        }

        return extName;
    }
}
