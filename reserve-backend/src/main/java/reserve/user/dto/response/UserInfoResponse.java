package reserve.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reserve.user.domain.User;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class UserInfoResponse {

    private final String username;
    private final String nickname;
    private final String description;
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
