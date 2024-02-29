package reserve.signup.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.signup.infrastructure.validator.Confirmation;
import reserve.signup.infrastructure.validator.Password;
import reserve.signup.infrastructure.validator.PasswordConfirmationCheck;

@NoArgsConstructor
@Getter
@Setter
@PasswordConfirmationCheck(targetClass = SignUpRequest.class)
public class SignUpRequest {

    @NotNull(message = "Username required.")
    @Size(min = 4, max = 25, message = "Username must be between {min} and {max} characters long.")
    private String username;

    @NotNull(message = "Password required.")
    @Size(min = 8, max = 50, message = "Password must be at least {min} characters long.")
    @Password
    private String password;

    @NotNull(message = "Password confirmation required.")
    @Confirmation
    private String passwordConfirmation;

    @NotNull(message = "Nickname required.")
    @Size(min = 2, max = 30, message = "Nickname must be between {min} and {max} characters long.")
    private String nickname;

}
