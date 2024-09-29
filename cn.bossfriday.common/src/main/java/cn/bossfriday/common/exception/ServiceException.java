package cn.bossfriday.common.exception;

/**
 * ServiceException
 *
 * @author chenx
 */
public class ServiceException extends Exception {

    public ServiceException(Exception e) {
        super(e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
