package reserve.global.exception;

public class InvalidAuthorizationException extends ErrorCodeException {

    public InvalidAuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidAuthorizationException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

}
