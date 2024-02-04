package reserve.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserUpdateRequest {

    @Size(min = 2, max = 30, message = "Nickname must be between {min} and {max} characters long.")
    private String nickname;

    private String description;

}
