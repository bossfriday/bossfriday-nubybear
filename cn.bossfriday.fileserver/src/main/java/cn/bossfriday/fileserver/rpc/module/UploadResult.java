package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.Data;

@Data
public class UploadResult {
    private String fileTransactionId;
    private OperationResult result;
    private MetaDataIndex metaDataIndex;

    public UploadResult() {

    }

    public UploadResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }

    public UploadResult(String fileTransactionId, OperationResult result, MetaDataIndex metaDataIndex) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
        this.metaDataIndex = metaDataIndex;
    }
}
