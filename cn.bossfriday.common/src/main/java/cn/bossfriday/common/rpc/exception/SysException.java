package cn.bossfriday.common.rpc.exception;

public class SysException extends Exception {
    private static final long serialVersionUID = 2621723872640262778L;

    public SysException(String msg) {
        super(msg);
    }

    public SysException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public  Throwable fillInStackTrace() {
        return this;
    }
}
