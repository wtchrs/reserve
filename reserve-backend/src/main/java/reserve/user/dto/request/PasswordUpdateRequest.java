package reserve.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.signup.infrastructure.validator.Confirmation;
import reserve.signup.infrastructure.validator.Password;
import reserve.signup.infrastructure.validator.PasswordConfirmationCheck;

@NoArgsConstructor
@Getter
@Setter
@PasswordConfirmationCheck(
        targetClass = PasswordUpdateRequest.class,
        message = "Not matched new password and new password confirmation."
)
public class PasswordUpdateRequest {

    @NotEmpty(message = "Current password required.")
    private String oldPassword;

    @NotNull(message = "New password required.")
    @Size(min = 8, max = 50, message = "Password must be at least {min} characters long.")
    @Password
    private String newPassword;

    @NotNull(message = "New password confirmation required.")
    @Confirmation
    private String confirmation;

}