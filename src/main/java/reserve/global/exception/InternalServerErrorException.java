package reserve.global.exception;

import lombok.Getter;

@Getter
public class InternalServerErrorException extends RuntimeException {

    private final ErrorCode errorCode;

    public InternalServerErrorException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public InternalServerErrorException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InternalServerErrorException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

}
