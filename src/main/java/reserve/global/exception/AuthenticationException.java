package reserve.global.exception;

public class AuthenticationException extends ErrorCodeException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

}
