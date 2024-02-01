package reserve.signin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SignInRequest {

    @NotEmpty(message = "Username required.")
    private String username;

    @NotEmpty(message = "Password required.")
    private String password;

}
