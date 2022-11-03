package cn.bossfriday.fileserver.common.enums;

import cn.bossfriday.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static cn.bossfriday.fileserver.common.FileServerConst.*;

/**
 * FileUploadType
 *
 * @author chenx
 */
@AllArgsConstructor
public enum FileUploadType {

    /**
     * 一次全量上传
     */
    FULL_UPLOAD(URL_UPLOAD_FULL),
    /**
     * base64一次全量上传
     */
    BASE_64_UPLOAD(URL_UPLOAD_BASE64),
    /**
     * 多次断点续传
     */
    RANGE_UPLOAD(URL_UPLOAD_RANGE);

    @Getter
    private String name;

    /**
     * getByName
     *
     * @param name
     * @return
     */
    public static FileUploadType getByName(String name) {
        for (FileUploadType fileUploadType : FileUploadType.values()) {
            if (fileUploadType.getName().equals(name.toLowerCase())) {
                return fileUploadType;
            }
        }

        throw new BizException("invalid FileUploadType, name:" + name + " !");
    }
}
