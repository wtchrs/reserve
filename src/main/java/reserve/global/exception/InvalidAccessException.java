package reserve.global.exception;

public class InvalidAccessException extends ErrorCodeException {

    public InvalidAccessException(ErrorCode errorCode) {
        super(errorCode);
    }

}
