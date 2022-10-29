package cn.bossfriday.fileserver.common.enums;

import lombok.Getter;

/**
 * OperationResult
 *
 * @author chenx
 */
public enum OperationResult {

    /**
     * 操作结果
     */
    OK(200, "ok"),

    BAD_REQUEST(400, "bad request"),

    NOT_FOUND(404, "not found"),

    SYSTEM_ERROR(500, "internal system error");

    OperationResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Getter
    private int code;

    @Getter
    private String msg;
}
