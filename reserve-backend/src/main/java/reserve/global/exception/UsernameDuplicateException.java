package reserve.global.exception;

public class UsernameDuplicateException extends ErrorCodeException {

    public UsernameDuplicateException(ErrorCode errorCode) {
        super(errorCode);
    }

}
