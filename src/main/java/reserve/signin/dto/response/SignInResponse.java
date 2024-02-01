package reserve.signin.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SignInResponse {

    private final String accessToken;

}
