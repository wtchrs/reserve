package reserve.signin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SignInRequest {

    @Schema(description = "Username", example = "username", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Username required.")
    private String username;

    @Schema(description = "Password", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Password required.")
    private String password;

}
