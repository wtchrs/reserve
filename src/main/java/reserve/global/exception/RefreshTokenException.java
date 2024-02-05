package reserve.global.exception;

public class RefreshTokenException extends ErrorCodeException {

    public RefreshTokenException(ErrorCode errorCode) {
        super(errorCode);
    }

}
