package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;

public interface ITmpFileHandler {
    /**
     * write
     *
     * @param msg
     * @return
     * @throws Exception
     */
    WriteTmpFileResult write(WriteTmpFileMsg msg) throws Exception;

    /**
     * rename
     *
     * @param transferCompletedTmpFilePath
     * @param recoverableTmpFileName
     * @return recoverableTmpFilePath
     * @throws Exception
     */
    String rename(String transferCompletedTmpFilePath, String recoverableTmpFileName) throws Exception;
}
