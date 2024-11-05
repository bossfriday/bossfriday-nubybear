package cn.bossfriday.im.common.message.file;

import cn.bossfriday.common.http.model.Range;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WriteTmpFileInput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteTmpFileInput {

    /**
     * storageEngineVersion
     */
    private int storageEngineVersion;

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * storageNamespace
     */
    private String storageNamespace;

    /**
     * isKeepAlive
     */
    private boolean isKeepAlive;

    /**
     * fileName
     */
    private String fileName;

    /**
     * range（断点上传用）
     */
    private Range range;

    /**
     * fileTotalSize
     */
    private long fileTotalSize;

    /**
     * offset
     */
    private long offset;

    /**
     * data
     */
    private byte[] data;

    /**
     * 是否断点上传
     *
     * @return
     */
    public boolean isRangeUpload() {
        return this.range != null;
    }
}
