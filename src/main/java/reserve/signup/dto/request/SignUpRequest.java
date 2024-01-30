package reserve.signup.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reserve.signup.infrastructure.validator.Confirmation;
import reserve.signup.infrastructure.validator.Password;
import reserve.signup.infrastructure.validator.PasswordConfirmationCheck;

@Getter
@NoArgsConstructor
@PasswordConfirmationCheck(targetClass = SignUpRequest.class)
public class SignUpRequest {

    @Size(min = 4, max = 25, message = "Username must be between {min} and {max} characters long.")
    private String username;

    @Size(min = 8, max = 50, message = "Password must be at least {min} characters long.")
    @Password
    private String password;

    @Confirmation
    private String passwordConfirmation;

    @Size(min = 4, max = 30, message = "Nickname must be between {min} and {max} characters long.")
    private String nickname;

}
