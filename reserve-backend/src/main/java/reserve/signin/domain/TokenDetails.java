package reserve.signin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TokenDetails {

    private final String userId;
    private final String username;
    private final String nickname;

}
