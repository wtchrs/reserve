package reserve.global.exception;

public class ResourceNotFoundException extends ErrorCodeException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

}
