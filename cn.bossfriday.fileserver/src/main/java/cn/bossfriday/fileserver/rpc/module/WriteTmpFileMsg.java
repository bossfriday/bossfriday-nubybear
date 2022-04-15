package cn.bossfriday.fileserver.rpc.module;

import lombok.Data;

@Data
public class WriteTmpFileMsg {
    private String fileTransactionId;
    private String fileName;
    private int fileSize;
    private long offset;
    private byte[] data;

    @Override
    public String toString() {
        return "WriteTmpFileMsg{" +
                "fileTransactionId='" + fileTransactionId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
