package cn.bossfriday.im.common.result;

import lombok.Getter;

import static cn.bossfriday.im.common.result.Result.OK_CODE;
import static cn.bossfriday.im.common.result.Result.OK_MSG;

/**
 * ResultCode
 *
 * @author chenx
 */
public enum ResultCode {

    /**
     * 公共错误码 0-500
     */
    OK(OK_CODE, OK_MSG),
    SYSTEM_ERROR(500, "System error!"),

    /**
     * API服务错误码 1000 - 1999
     */
    API_REQUEST_URI_ERROR(1000, "Reqeust URI Error!");

    @Getter
    private int code;

    @Getter
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
