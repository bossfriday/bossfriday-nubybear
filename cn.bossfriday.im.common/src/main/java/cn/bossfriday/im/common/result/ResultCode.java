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
     * OK
     */
    OK(OK_CODE, OK_MSG),

    /**
     * API服务错误码 1000 - 1999
     */
    API_UNSUPPORTED(1000, "Unsupported API!"),
    API_AUTHENTICATION_FAILED(1001, "API Authentication Failed!"),

    /**
     * 公共错误码 9000 - 9999
     */
    APP_NOT_EXISTED_OR_INVALID(9000, "App Not Existed Or Invalid!"),
    SYSTEM_ERROR(9999, "System Error!"),
    ;

    @Getter
    private int code;

    @Getter
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
