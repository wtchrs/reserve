package reserve.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reserve.user.domain.User;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class UserInfoResponse {

    @Schema(description = "Username", example = "username")
    private final String username;

    @Schema(description = "Nickname", example = "nickname")
    private final String nickname;

    @Schema(description = "Description", example = "description")
    private final String description;

    @Schema(description = "Sign-up date", example = "2021-01-01")
    private final LocalDate signUpDate;

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getNickname(),
                user.getDescription(),
                user.getCreatedAt().toLocalDate()
        );
    }

}
