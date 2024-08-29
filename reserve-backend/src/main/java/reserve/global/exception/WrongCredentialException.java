package reserve.global.exception;

public class WrongCredentialException extends ErrorCodeException {

    public WrongCredentialException(ErrorCode errorCode) {
        super(errorCode);
    }

}
