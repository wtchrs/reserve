package reserve.global.exception;

public class InvalidAuthorizationException extends ErrorCodeException {

    public InvalidAuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }

}
