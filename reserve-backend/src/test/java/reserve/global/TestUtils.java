package reserve.global;

import reserve.signin.domain.TokenDetails;
import reserve.user.domain.User;

public final class TestUtils {

    public static TokenDetails getTokenDetails(User user) {
        return new TokenDetails(user.getId().toString(), user.getUsername(), user.getNickname());
    }

    public static TokenDetails getTokenDetails(Long userId) {
        return new TokenDetails(userId.toString(), "user", "User");
    }

}
