package reserve.global.exception;

import lombok.Getter;

@Getter
public class UsernameDuplicateException extends RuntimeException {

    private final ErrorCode errorCode;

    public UsernameDuplicateException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
