package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;

public interface ITmpFileHandler {
    /**
     * write
     */
    WriteTmpFileResult write(WriteTmpFileMsg msg) throws Exception;
}
