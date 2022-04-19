package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import lombok.Data;

@Data
public class WriteTmpFileResp {
    private int code;
    private String msg;
    private String fileTransactionId;
    private int fileSize;
    private int savedThunkSize;

    public WriteTmpFileResp() {

    }

    public WriteTmpFileResp(String fileTransactionId, OperationResult result) {
        this.code = result.getCode();
        this.msg = result.getMsg();
        this.fileTransactionId = fileTransactionId;
    }

    /**
     * 写临时文件是否完成
     */
    public boolean isCompleted() throws Exception {
        return this.savedThunkSize == fileSize;
    }
}
