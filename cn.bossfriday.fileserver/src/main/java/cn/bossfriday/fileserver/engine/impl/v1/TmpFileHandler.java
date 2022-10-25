package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.Func;
import cn.bossfriday.common.utils.LruHashMap;
import cn.bossfriday.fileserver.common.conf.FileServerConfigManager;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
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
@CurrentStorageEngineVersion
public class TmpFileHandler implements ITmpFileHandler {
    private LruHashMap<String, FileChannel> tmpFileChannelMap = new LruHashMap<>(5000, new Func.Action2<String, FileChannel>() {

        @Override
        public void invoke(String arg1, FileChannel arg2) {
            try {
                arg2.close();
            } catch (Exception ex) {
                // ignore
            }
        }
    }, 1000 * 60 * 30L);

    @Override
    public WriteTmpFileResult write(WriteTmpFileMsg msg) throws Exception {
        if (msg == null) {
            throw new BizException("WriteTmpFileMsg is null!");
        }

        String fileTransactionId = msg.getFileTransactionId();
        FileChannel tmpFileAccess = null;
        WriteTmpFileResult result = null;
        int chunkedDataSize = msg.getData().length;
        if (chunkedDataSize == 0) {
            log.warn("chunkedDataSize=0: " + fileTransactionId);
            return null;
        }

        try {
            FileTransactionContext ctx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
            if (ctx == null) {
                throw new Exception("FileTransactionContext is null!(" + fileTransactionId + ")");
            }

            tmpFileAccess = this.getTmpFileChannel(msg);
            FileUtil.transferFrom(tmpFileAccess, msg.getData(), msg.getOffset());
            long savedSize = ctx.addAndGetTransferredSize(msg.getData().length);

            // 临时文件完成
            if (savedSize >= msg.getFileTotalSize()) {
                tmpFileAccess.close();
                this.tmpFileChannelMap.remove(fileTransactionId);

                result = new WriteTmpFileResult();
                renameTmpFile(msg, result);
                result.setFileTransactionId(msg.getFileTransactionId());
                result.setResult(OperationResult.OK);
                result.setStorageEngineVersion(msg.getStorageEngineVersion());
                result.setNamespace(msg.getNamespace());
                result.setClusterNodeName(FileServerConfigManager.getCurrentClusterNodeName());
                result.setKeepAlive(msg.isKeepAlive());
                result.setTimestamp(System.currentTimeMillis());
                result.setFileTotalSize(msg.getFileTotalSize());
                result.setFileName(msg.getFileName());
                log.info("tmpFile process done :" + fileTransactionId);
            }
        } catch (Exception ex) {
            log.error("write tmpFile error!", ex);
            if (tmpFileAccess != null) {
                tmpFileAccess.close();
                this.tmpFileChannelMap.remove(fileTransactionId);
            }

            result = new WriteTmpFileResult(fileTransactionId, OperationResult.SystemError);
        }

        return result;
    }

    @Override
    public String rename(String transferCompletedTmpFilePath, String recoverableTmpFileName) throws Exception {
        File oldFile = new File(transferCompletedTmpFilePath);
        if (!oldFile.exists()) {
            throw new BizException("TmpFile not existed: " + transferCompletedTmpFilePath);
        }

        File tmpDir = StorageEngine.getInstance().getTmpDir();
        File newFile = new File(tmpDir, recoverableTmpFileName);
        if (newFile.exists()) {
            throw new BizException("RecoverableTmpFile already existed: " + recoverableTmpFileName);
        }

        if (!oldFile.renameTo(newFile)) {
            throw new Exception("rename RecoverableTmpFile failed: " + recoverableTmpFileName);
        }

        return newFile.getAbsolutePath();
    }

    @Override
    public boolean deleteIngTmpFile(String fileTransactionId) {
        File tmpFile = getTmpFile(fileTransactionId);
        if (tmpFile.exists()) {
            boolean b = tmpFile.delete();
            if (b) {
                log.info("deleteIngTmpFile done: " + fileTransactionId);
            }
        }

        return true;
    }

    private synchronized FileChannel getTmpFileChannel(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        if (this.tmpFileChannelMap.containsKey(fileTransactionId)) {
            return this.tmpFileChannelMap.get(fileTransactionId);
        }

        File tmpFile = getTmpFile(fileTransactionId);
        if (!tmpFile.exists()) {
            FileUtil.create(tmpFile, msg.getFileSize());
        }

        FileChannel destFileChannel = new RandomAccessFile(tmpFile, "rw").getChannel();
        this.tmpFileChannelMap.put(fileTransactionId, destFileChannel);

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
     */
    private static void renameTmpFile(WriteTmpFileMsg msg, WriteTmpFileResult result) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        File tmpFile = getTmpFile(fileTransactionId);
        String extName = FileUtil.getFileExt(msg.getFileName()).toLowerCase();
        if (StringUtils.isEmpty(extName)) {
            extName = FILE_DEFAULT_EXT;
        }

        result.setFileExtName(extName);
        String newFilePath = tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().lastIndexOf(".")) + "." + extName;
        File newFile = new File(newFilePath);
        if (!newFile.exists()) {
            if (!tmpFile.renameTo(newFile)) {
                throw new Exception("rename tmpFile failed: " + fileTransactionId);
            }
        }

        result.setFilePath(newFile.getAbsolutePath());
    }
}
