package cn.bossfriday.fileserver.common.enums;

/**
 * FileUploadType
 *
 * @author chenx
 */
public enum FileUploadType {
    
    /**
     * 完整上传
     */
    FULL_UPLOAD,
    /**
     * base64上传
     */
    BASE_64_UPLOAD,
    /**
     * 断点续传
     */
    RANGE_UPLOAD
}
