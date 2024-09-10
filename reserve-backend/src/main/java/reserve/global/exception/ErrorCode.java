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
    INVALID_REFRESH_TOKEN(104, "The refresh token is invalid."),

    WRONG_CREDENTIAL(110, "Username or password is incorrect."),
    WRONG_PASSWORD(111, "Password is incorrect."),
    SIGN_IN_REQUIRED(112, "Please sign in to continue."),

    EXPIRED_ACCESS_TOKEN(120, "Access token is expired."),
    EXPIRED_REFRESH_TOKEN(121, "Refresh token is expired."),

    // 2xx: Wrong request.
    INVALID_REQUEST(200, "Request is invalid."),
    RESERVATION_CANNOT_CANCEL(211, "Failed to cancel the reservation as it is already in service or completed."),
    RESERVATION_CANNOT_START(212, "Failed to start the reservation as it is not ready state."),
    RESERVATION_CANNOT_COMPLETE(213, "Failed to complete the reservation as it is not in service."),

    // 3xx: Not found errors.
    USER_NOT_FOUND(301, "User does not exist."),
    STORE_NOT_FOUND(302, "Store does not exist."),
    MENU_NOT_FOUND(303, "Menu does not exist."),
    RESERVATION_NOT_FOUND(304, "Reservation does not exist."),
    NOTIFICATION_NOT_FOUND(305, "Notification does not exist."),

    // 4xx: Access denied errors.
    ACCESS_DENIED(400, "Access denied."),

    // 5xx: Conflict errors.
    USERNAME_DUPLICATE(501, "Username already exists."),

    // 9xx: server errors.
    INTERNAL_SERVER_ERROR(900, "An internal server error has occurred. Please try again later.");

    private final int code;
    private final String message;

    @Override
    public String toString() {
        return this.code + ": " + this.message;
    }

}