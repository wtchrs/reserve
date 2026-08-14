package reserve.signin.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reserve.support.IntegrationTest;
import reserve.signin.domain.RefreshToken;

@IntegrationTest
class RefreshTokenRepositoryTest {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
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
