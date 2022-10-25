package cn.bossfriday.common.exception;

/**
 * BizException
 *
 * @author chenx
 */
public class BizException extends RuntimeException {

    public BizException(RuntimeException e) {
        super(e);
    }

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String msg, RuntimeException e) {
        super(msg, e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
