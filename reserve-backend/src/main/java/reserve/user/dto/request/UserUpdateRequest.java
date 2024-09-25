package reserve.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {

    @Schema(description = "New nickname", example = "nickname", requiredMode = RequiredMode.NOT_REQUIRED)
    @Size(min = 2, max = 30, message = "Nickname must be between {min} and {max} characters long.")
    private String nickname;

    @Schema(description = "New description", example = "description", requiredMode = RequiredMode.NOT_REQUIRED)
    private String description;

}
