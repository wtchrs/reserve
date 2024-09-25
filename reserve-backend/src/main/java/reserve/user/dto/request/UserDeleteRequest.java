package reserve.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserDeleteRequest {

    @Schema(description = "Password", example = "password1234", requiredMode = RequiredMode.REQUIRED)
    @NotEmpty
    private String password;

}
