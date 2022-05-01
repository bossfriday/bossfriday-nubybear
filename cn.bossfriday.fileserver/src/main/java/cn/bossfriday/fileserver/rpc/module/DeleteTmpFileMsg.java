package cn.bossfriday.fileserver.rpc.module;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteTmpFileMsg {
    private int storageEngineVersion;
    private String fileTransactionId;

    public DeleteTmpFileMsg() {

    }

    public DeleteTmpFileMsg(int storageEngineVersion, String fileTransactionId) {
        this.storageEngineVersion = storageEngineVersion;
        this.fileTransactionId = fileTransactionId;
    }
}
