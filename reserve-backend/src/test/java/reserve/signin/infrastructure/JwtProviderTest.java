package reserve.signin.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserve.global.MutableClock;
import reserve.global.TestUtils;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.domain.TokenDetails;
import reserve.signin.dto.SignInToken;

class JwtProviderTest {

    final String ACCESS_TOKEN_SECRET = "1234567890123456789012345678901234567890123456789012345678901234";

    final String REFRESH_TOKEN_SECRET = "9876543210987654321098765432109876543210987654321098765432109876";

    final int REFRESH_TOKEN_EXPIRATION = 604800;

    final int ACCESS_TOKEN_EXPIRATION = 600;

    JwtProvider jwtProvider = new JwtProvider(ACCESS_TOKEN_SECRET, REFRESH_TOKEN_SECRET, ACCESS_TOKEN_EXPIRATION,
            REFRESH_TOKEN_EXPIRATION, Clock.systemUTC());

    @Test
    @DisplayName("Testing sign-in token generation")
    void testSignInTokenGeneration() {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L));

        assertNotNull(signInToken.getAccessToken());
        assertNotNull(signInToken.getRefreshToken());
    }

    @Test
    @DisplayName("Testing access token expiration")
    void testAccessTokenExpiration() {
        assertThrows(InvalidAuthorizationException.class, () -> jwtProvider.isAccessTokenExpired("Not a valid token"));
        assertFalse(jwtProvider
            .isAccessTokenExpired(jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L)).getAccessToken()));
        assertTrue(jwtProvider.isAccessTokenExpired(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzA2NjY5NjM4LCJpYXQiOjE3MDc2Njk2Mzh9.t5NwbYdiymRw_jQCkV2TqjL3iWGtmpQ3sNVkyVmSi_6o8wN7RSYvg-g0T5Ux8SoILQv_B3wQWYvkuDF13UM_Pw"));
    }

    @Test
    @DisplayName("Testing refresh token expiration")
    void testRefreshTokenExpiration() {
        assertThrows(InvalidAuthorizationException.class, () -> jwtProvider.isRefreshTokenExpired("Not a valid token"));
        assertFalse(jwtProvider
            .isRefreshTokenExpired(jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L)).getRefreshToken()));
        assertTrue(jwtProvider.isRefreshTokenExpired(
                "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzA2NjY5NjM4LCJpYXQiOjE3MDc2Njk2Mzh9.ToGdMnUdbpzwMkj5uju18AKb1c5m__99hhieTuIgTFc94Wot1ANbN7sd1nzKJgtBkIbMK5N8iR9874c2S8QzHA"));
    }

    @Test
    @DisplayName("Testing subject extraction from token")
    void testSubjectExtraction() {
        // userId: "1", username: "user", nickname: "User"
        TokenDetails tokenDetails = jwtProvider
            .extractAccessTokenDetails(jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L)).getAccessToken());
        assertEquals("1", tokenDetails.getUserId());
        assertEquals("user", tokenDetails.getUsername());
        assertEquals("User", tokenDetails.getNickname());
    }

    @Test
    void generateSignInToken_setsConfiguredValidityDurations() {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L));

        Claims accessTokenClaims = getTokenClaims(ACCESS_TOKEN_SECRET, signInToken.getAccessToken());
        assertEquals(ACCESS_TOKEN_EXPIRATION * 1000,
                accessTokenClaims.getExpiration().getTime() - accessTokenClaims.getIssuedAt().getTime());

        Claims refreshTokenClaims = getTokenClaims(REFRESH_TOKEN_SECRET, signInToken.getRefreshToken());
        assertEquals(REFRESH_TOKEN_EXPIRATION * 1000,
                refreshTokenClaims.getExpiration().getTime() - refreshTokenClaims.getIssuedAt().getTime());
    }

    private Claims getTokenClaims(String secret, String token) {
        return Jwts.parserBuilder()
            .setSigningKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    JwtProvider.SIGNATURE_ALGORITHM.getJcaName()))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    @Test
    void accessToken_expiresAfterConfiguredSeconds() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtProvider provider = new JwtProvider(ACCESS_TOKEN_SECRET, REFRESH_TOKEN_SECRET, ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION, clock);

        String token = provider.generateSignInToken(TestUtils.getTokenDetails(1L)).getAccessToken();

        // one second before expiration
        clock.advance(Duration.ofSeconds(ACCESS_TOKEN_EXPIRATION - 1));
        assertFalse(provider.isAccessTokenExpired(token));

        // one second after expiration
        clock.advance(Duration.ofSeconds(2));
        assertTrue(provider.isAccessTokenExpired(token));
    }

    @Test
    void refreshToken_expiresAfterConfiguredSeconds() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtProvider provider = new JwtProvider(ACCESS_TOKEN_SECRET, REFRESH_TOKEN_SECRET, ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION, clock);

        String token = provider.generateSignInToken(TestUtils.getTokenDetails(1L)).getRefreshToken();

        // one second before expiration
        clock.advance(Duration.ofSeconds(REFRESH_TOKEN_EXPIRATION - 1));
        assertFalse(provider.isRefreshTokenExpired(token));

        // one second after expiration
        clock.advance(Duration.ofSeconds(2));
        assertTrue(provider.isRefreshTokenExpired(token));
    }

}
