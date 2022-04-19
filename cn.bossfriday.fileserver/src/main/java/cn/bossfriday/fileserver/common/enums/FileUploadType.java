package cn.bossfriday.fileserver.common.enums;

public enum FileUploadType {
    FullUpload,     // 完整上传
    Base64Upload,   // base64上传
    RangeUpload,    // 断点续传
}
