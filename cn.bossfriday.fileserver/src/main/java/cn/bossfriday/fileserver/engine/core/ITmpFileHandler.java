package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResp;

public interface ITmpFileHandler {
    /**
     * write
     */
    WriteTmpFileResp write(WriteTmpFileMsg msg) throws Exception;
}
