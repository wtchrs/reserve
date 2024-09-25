package reserve.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
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
@PasswordConfirmationCheck(
        targetClass = PasswordUpdateRequest.class,
        message = "Not matched new password and new password confirmation."
)
public class PasswordUpdateRequest {

    @Schema(description = "Current password", example = "password1234", requiredMode = RequiredMode.REQUIRED)
    @NotEmpty(message = "Current password required.")
    private String oldPassword;

    @Schema(description = "New password", example = "newPassword1234", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "New password required.")
    @Size(min = 8, max = 50, message = "Password must be at least {min} characters long.")
    @Password
    private String newPassword;

    @Schema(
            description = "New password confirmation",
            example = "newPassword1234",
            requiredMode = RequiredMode.REQUIRED
    )
    @NotNull(message = "New password confirmation required.")
    @Confirmation
    private String confirmation;

}