package reserve.signin.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.dto.SignInToken;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    final String ACCESS_TOKEN_SECRET = "1234567890123456789012345678901234567890123456789012345678901234";
    final String REFRESH_TOKEN_SECRET = "9876543210987654321098765432109876543210987654321098765432109876";

    JwtProvider jwtProvider = new JwtProvider(ACCESS_TOKEN_SECRET, REFRESH_TOKEN_SECRET, 600, 604800);

    @Test
    @DisplayName("Testing sign-in token generation")
    void testSignInTokenGeneration() {
        SignInToken signInToken = jwtProvider.generateSignInToken("1");

        assertNotNull(signInToken.getAccessToken());
        assertNotNull(signInToken.getRefreshToken());
    }

    @Test
    @DisplayName("Testing access token expiration")
    void testAccessTokenExpiration() {
        assertThrows(InvalidAuthorizationException.class, () -> jwtProvider.isAccessTokenExpired("Not a valid token"));
        assertFalse(jwtProvider.isAccessTokenExpired(jwtProvider.generateSignInToken("1").getAccessToken()));
        assertTrue(jwtProvider.isAccessTokenExpired(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzA2NjY5NjM4LCJpYXQiOjE3MDc2Njk2Mzh9.t5NwbYdiymRw_jQCkV2TqjL3iWGtmpQ3sNVkyVmSi_6o8wN7RSYvg-g0T5Ux8SoILQv_B3wQWYvkuDF13UM_Pw"));
    }

    @Test
    @DisplayName("Testing refresh token expiration")
    void testRefreshTokenExpiration() {
        assertThrows(InvalidAuthorizationException.class, () -> jwtProvider.isRefreshTokenExpired("Not a valid token"));
        assertFalse(jwtProvider.isRefreshTokenExpired(jwtProvider.generateSignInToken("1").getRefreshToken()));
        assertTrue(jwtProvider.isRefreshTokenExpired(
                "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzA2NjY5NjM4LCJpYXQiOjE3MDc2Njk2Mzh9.ToGdMnUdbpzwMkj5uju18AKb1c5m__99hhieTuIgTFc94Wot1ANbN7sd1nzKJgtBkIbMK5N8iR9874c2S8QzHA"));
    }

    @Test
    @DisplayName("Testing subject extraction from token")
    void testSubjectExtraction() {
        assertEquals("1", jwtProvider.extractAccessTokenSubject(jwtProvider.generateSignInToken("1").getAccessToken()));
    }

}
