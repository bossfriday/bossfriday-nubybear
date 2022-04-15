package cn.bossfriday.fileserver.common.entity;

import lombok.Getter;

/**
 * TODO：考虑后续大文件断点续传使用
 */
public class BrokenUploadRequestHeader {
    private String range;

    @Getter
    private String fileTransactionId;

    @Getter
    private int fileTotalSize = 0;

    public BrokenUploadRequestHeader(String range, String fileTransactionId, int fileTotalSize) {
        this.range = range;
        this.fileTransactionId = fileTransactionId;
        this.fileTotalSize = fileTotalSize;
    }

    public RangeInfo getRange() {
        return new RangeInfo(this.range);
    }
}
