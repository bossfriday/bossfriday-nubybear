package cn.bossfriday.fileserver.rpc.module;

import lombok.Data;

@Data
public class WriteTmpFileResult {
    private String fileTransactionId;
    private int fileSize;
    private int savedThunkSize;

    /**
     * 写临时文件是否完成
     */
    public boolean isCompleted() throws Exception {
        return this.savedThunkSize == fileSize;
    }
}
