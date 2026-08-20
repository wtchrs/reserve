package reserve.signin.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import reserve.support.IntegrationTest;
import reserve.signin.domain.RefreshToken;
import reserve.support.RedisCleaner;

@IntegrationTest
@Import(RedisCleaner.class)
class RefreshTokenRepositoryTest {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    RedisCleaner redisCleaner;

    @AfterEach
    void cleanUp() {
        redisCleaner.cleanUp();
    }

    @Test
    @DisplayName("Testing RefreshToken persistence and retrieval")
    void testRefreshTokenPersistence() {
        refreshTokenRepository.save(new RefreshToken("token1", 1L, 604800));
        refreshTokenRepository.findById("token1").ifPresentOrElse(refreshToken -> {
            assertEquals("token1", refreshToken.getTokenValue());
            assertEquals(1L, refreshToken.getUserId());
            assertEquals(604800, refreshToken.getExpiration());
        }, () -> fail("RefreshToken not found"));
    }

}
