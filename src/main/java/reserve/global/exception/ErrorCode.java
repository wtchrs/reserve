package reserve.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // 1xx: Authentication and authorization errors.
    INVALID_ACCESS_TOKEN_FORMAT(101, "The Access token format is invalid."),
    INVALID_REFRESH_TOKEN_FORMAT(102, "The refresh token format is invalid."),
    INVALID_SIGN_IN_INFO(103, "Sign-in information is invalid."),

    WRONG_CREDENTIAL(110, "Username or password is incorrect."),
    WRONG_PASSWORD(111, "Password is incorrect."),
    SIGN_IN_REQUIRED(112, "Please sign in to continue."),

    EXPIRED_ACCESS_TOKEN(120, "Access token is expired."),
    EXPIRED_REFRESH_TOKEN(121, "Refresh token is expired."),

    // 2xx: Wrong request format errors.
    INVALID_REQUEST(200, "Request is invalid."),

    // 3xx: Not found errors.
    USER_NOT_FOUND(301, "User does not exist."),
    ROOM_NOT_FOUND(302, "Room does not exist."),

    // 4xx: Conflict errors.
    USERNAME_DUPLICATE(401, "Username already exists."),

    // 9xx: server errors.
    INTERNAL_SERVER_ERROR(900, "An internal server error has occurred. Please try again later.");

    private final int code;
    private final String message;

    @Override
    public String toString() {
        return this.code + ": " + this.message;
    }

}