package reserve.global.exception;

public class AccessTokenException extends ErrorCodeException {

    public AccessTokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AccessTokenException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

}
