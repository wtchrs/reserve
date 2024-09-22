package reserve.signup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Username", example = "username", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Username required.")
    @Size(min = 4, max = 25, message = "Username must be between {min} and {max} characters long.")
    private String username;

    @Schema(description = "Password", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Password required.")
    @Size(min = 8, max = 50, message = "Password must be at least {min} characters long.")
    @Password
    private String password;

    @Schema(description = "Password confirmation", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Password confirmation required.")
    @Confirmation
    private String passwordConfirmation;

    @Schema(description = "Nickname", example = "nickname", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Nickname required.")
    @Size(min = 2, max = 30, message = "Nickname must be between {min} and {max} characters long.")
    private String nickname;

}
