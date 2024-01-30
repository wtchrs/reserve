package reserve.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // 1xx: Wrong request format errors.
    INVALID_REQUEST(100, "Request is invalid."),

    // 2xx: Not found errors.
    USER_NOT_FOUND(201, "User does not exist."),

    // 3xx: Conflict errors.
    USERNAME_DUPLICATE(301, "Username already exists."),

    // 9xx: server errors.
    INTERNAL_SERVER_ERROR(900, "An internal server error has occurred. Please try again later.")
    ;

    private final int code;
    private final String message;

}