package cn.bossfriday.im.common.entity.result;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;

/**
 * Result
 *
 * @author chenx
 */
public class Result<T> {

    public static final int OK_CODE = 0;
    public static final String OK_MSG = "OK";

    @Getter
    private int code;

    @Getter
    private String msg;

    @Getter
    private T data;

    public Result() {

    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * ok
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(OK_CODE, OK_MSG, data);
    }

    /**
     * error
     *
     * @param code
     * @param msg
     * @return
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg());
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
