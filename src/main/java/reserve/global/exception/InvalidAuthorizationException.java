package reserve.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidAuthorizationException extends RuntimeException {

    private final ErrorCode errorCode;

}
