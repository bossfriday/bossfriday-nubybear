package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.actors.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.actors.module.WriteTmpFileResult;

/**
 * ITmpFileHandler
 *
 * @author chenx
 */
public interface ITmpFileHandler {

    /**
     * write
     *
     * @param msg
     * @return
     */
    WriteTmpFileResult write(WriteTmpFileMsg msg);

    /**
     * rename
     *
     * @param transferCompletedTmpFilePath
     * @param recoverableTmpFileName
     * @return
     */
    String rename(String transferCompletedTmpFilePath, String recoverableTmpFileName);

    /**
     * deleteIngTmpFile（上传意外中断删除ing临时文件）
     *
     * @param fileTransactionId
     * @return
     */
    boolean deleteIngTmpFile(String fileTransactionId);
}
