package reserve.signin.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("refresh")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefreshToken {

    @Id
    private String tokenValue;
    private Long userId;

    @TimeToLive
    private int expiration;

    public RefreshToken(String tokenValue, Long userId, int expiration) {
        this.tokenValue = tokenValue;
        this.userId = userId;
        this.expiration = expiration;
    }

}
