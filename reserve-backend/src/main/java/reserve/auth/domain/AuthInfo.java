package reserve.auth.domain;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Hidden
public class AuthInfo {

    private static final AuthInfo GUEST_PLACEHOLDER = new AuthInfo(0L, AuthType.GUEST);
    private static final AuthInfo EXPIRED_PLACEHOLDER = new AuthInfo(0L, AuthType.GUEST);

    private final Long userId;
    private final AuthType authType;

    public static AuthInfo guest() {
        return guest(false);
    }

    public static AuthInfo guest(boolean expired) {
        return expired ? EXPIRED_PLACEHOLDER : GUEST_PLACEHOLDER;
    }

    public static AuthInfo user(Long userId) {
        return new AuthInfo(userId, AuthType.USER);
    }

    public boolean isGuest() {
        return authType.equals(AuthType.GUEST);
    }

    public boolean isExpired() {
        return EXPIRED_PLACEHOLDER.equals(this);
    }

}
