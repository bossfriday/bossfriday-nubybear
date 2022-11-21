package cn.bossfriday.fileserver.common.enums;

import io.netty.handler.codec.http.HttpResponseStatus;
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
    OK(200, "ok", HttpResponseStatus.OK),

    BAD_REQUEST(400, "bad request", HttpResponseStatus.BAD_REQUEST),

    NOT_FOUND(404, "not found", HttpResponseStatus.NOT_FOUND),

    SYSTEM_ERROR(500, "internal system error", HttpResponseStatus.INTERNAL_SERVER_ERROR);

    OperationResult(int code, String msg, HttpResponseStatus status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

    @Getter
    private int code;

    @Getter
    private String msg;

    @Getter
    private HttpResponseStatus status;
}
