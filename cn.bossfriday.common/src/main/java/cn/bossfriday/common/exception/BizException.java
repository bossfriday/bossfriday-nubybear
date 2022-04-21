package cn.bossfriday.common.exception;

public class BizException extends Exception {
    public BizException(Exception e) {
        super(e);
    }

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String msg, Exception e) {
        super(msg, e);
    }

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}
}
