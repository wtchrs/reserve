package reserve.signin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SignInToken {

    private final String accessToken;
    private final String refreshToken;

}
