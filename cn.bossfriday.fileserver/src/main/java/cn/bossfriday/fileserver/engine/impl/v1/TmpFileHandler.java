package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.combo.Combo2;
import cn.bossfriday.common.conf.ServiceConfigManager;
import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.LruHashMap;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileMsg;
import cn.bossfriday.fileserver.actors.model.WriteTmpFileResult;
import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

/**
 * TmpFileHandler
 *
 * @author chenx
 */
@Slf4j
@CurrentStorageEngineVersion
public class TmpFileHandler implements ITmpFileHandler {
    private LruHashMap<String, FileChannel> tmpFileChannelMap = new LruHashMap<>(5000, (fileTransactionId, fileChannel) -> {
        try {
            fileChannel.close();
        } catch (Exception ex) {
            log.error("FileChannel close failed! fileTransactionId: " + fileTransactionId + ")", ex);
        }
    }, DEFAULT_LRU_DURATION);

    @Override
    public WriteTmpFileResult write(WriteTmpFileMsg msg) {
        if (msg == null) {
            throw new ServiceRuntimeException("WriteTmpFileMsg is null!");
        }

        String fileTransactionId = msg.getFileTransactionId();
        FileChannel tmpFileChannel = null;
        WriteTmpFileResult result = null;
        int chunkedDataSize = msg.getData().length;
        if (chunkedDataSize == 0) {
            return null;
        }

        try {
            FileTransactionContext ctx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
            if (ctx == null) {
                throw new ServiceRuntimeException("FileTransactionContext is null!(" + fileTransactionId + ")");
            }

            tmpFileChannel = this.getTmpFileChannel(msg);
            FileUtil.transferFrom(tmpFileChannel, msg.getData(), msg.getOffset());
            Combo2<Long, Long> tempFileSavedCounter = ctx.addAndGetTempFileSavedCounter(chunkedDataSize, msg.getRange());
            long rangeSavedCounterValue = tempFileSavedCounter.getV1();
            long tempFileSavedCounterValue = tempFileSavedCounter.getV2();

            // 临时文件完成
            if (tempFileSavedCounterValue == msg.getFileTotalSize()) {
                tmpFileChannel.close();
                this.tmpFileChannelMap.remove(fileTransactionId);
                result = getWriteTmpFileResult(msg, true);
                renameTmpFile(msg, result);
                log.info("tmpFile process done :" + fileTransactionId);
            } else if (msg.isRangeUpload() && rangeSavedCounterValue == msg.getRange().getLength()) {
                // 断点续传分片写入完成
                result = getWriteTmpFileResult(msg, false);
                log.info("range process done ,fileTransactionId: " + fileTransactionId + ", range:" + msg.getRange().toString());
            }
        } catch (Exception ex) {
            log.error("write tmpFile error!", ex);
            if (tmpFileChannel != null) {
                try {
                    tmpFileChannel.close();
                } catch (IOException e) {
                    log.error("tmpFileChannel close failed!", e);
                }

                this.tmpFileChannelMap.remove(fileTransactionId);
            }

            result = new WriteTmpFileResult(fileTransactionId, OperationResult.SYSTEM_ERROR);
        }

        return result;
    }

    @Override
    public String rename(String transferCompletedTmpFilePath, String recoverableTmpFileName) {
        File oldFile = new File(transferCompletedTmpFilePath);
        if (!oldFile.exists()) {
            throw new ServiceRuntimeException("TmpFile not existed: " + transferCompletedTmpFilePath);
        }

        File tmpDir = StorageEngine.getInstance().getTmpDir();
        File newFile = new File(tmpDir, recoverableTmpFileName);
        if (newFile.exists()) {
            throw new ServiceRuntimeException("RecoverableTmpFile already existed: " + recoverableTmpFileName);
        }

        if (!oldFile.renameTo(newFile)) {
            throw new ServiceRuntimeException("rename RecoverableTmpFile failed: " + recoverableTmpFileName);
        }

        return newFile.getAbsolutePath();
    }

    @Override
    public boolean deleteIngTmpFile(String fileTransactionId) {
        File tmpFile = getTmpFile(fileTransactionId);
        if (tmpFile.exists()) {
            try {
                Files.delete(tmpFile.toPath());
            } catch (IOException ex) {
                log.error("TempFileHandler.deleteIngTmpFile() error!", ex);
                return false;
            }
        }

        return true;
    }

    /**
     * getTmpFileChannel
     *
     * @param msg
     * @return
     * @throws IOException
     */
    @SuppressWarnings("squid:S2095")
    private synchronized FileChannel getTmpFileChannel(WriteTmpFileMsg msg) throws IOException {
        String fileTransactionId = msg.getFileTransactionId();
        if (this.tmpFileChannelMap.containsKey(fileTransactionId)) {
            return this.tmpFileChannelMap.get(fileTransactionId);
        }

        File tmpFile = getTmpFile(fileTransactionId);
        if (!tmpFile.exists()) {
            FileUtil.create(tmpFile, msg.getFileTotalSize());
        }

        FileChannel destFileChannel = new RandomAccessFile(tmpFile, "rw").getChannel();
        this.tmpFileChannelMap.put(fileTransactionId, destFileChannel);

        return destFileChannel;
    }

    /**
     * getTmpFile
     *
     * @param fileTransactionId
     * @return
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
     * @param result
     */
    private static void renameTmpFile(WriteTmpFileMsg msg, WriteTmpFileResult result) {
        String fileTransactionId = msg.getFileTransactionId();
        File tmpFile = getTmpFile(fileTransactionId);
        String extName = FileUtil.getFileExt(msg.getFileName()).toLowerCase();
        if (StringUtils.isEmpty(extName)) {
            extName = FILE_DEFAULT_EXT;
        }

        result.setFileExtName(extName);
        String newFilePath = tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().lastIndexOf(".")) + "." + extName;
        File newFile = new File(newFilePath);
        if (newFile.exists()) {
            throw new ServiceRuntimeException("newTmpFile already existed! path:" + newFilePath);
        }

        if (!tmpFile.renameTo(newFile)) {
            throw new ServiceRuntimeException("rename tmpFile failed: " + fileTransactionId);
        }

        result.setFilePath(newFile.getAbsolutePath());
    }

    /**
     * getWriteTmpFileResult
     *
     * @param msg
     * @param isFullDone
     * @return
     */
    private static WriteTmpFileResult getWriteTmpFileResult(WriteTmpFileMsg msg, boolean isFullDone) {
        if (msg == null) {
            throw new ServiceRuntimeException("the input WriteTmpFileMsg is null!");
        }

        WriteTmpFileResult result = new WriteTmpFileResult();
        result.setFileTransactionId(msg.getFileTransactionId());
        result.setResult(OperationResult.OK);
        result.setStorageEngineVersion(msg.getStorageEngineVersion());
        result.setStorageNamespace(msg.getStorageNamespace());
        result.setClusterNodeName(ServiceConfigManager.getClusterNodeName());
        result.setKeepAlive(msg.isKeepAlive());
        result.setTimestamp(System.currentTimeMillis());
        result.setFileTotalSize(msg.getFileTotalSize());
        result.setFileName(msg.getFileName());
        result.setRange(isFullDone ? null : msg.getRange());

        return result;
    }
}
