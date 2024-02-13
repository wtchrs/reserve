package reserve.signin.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.dto.SignInToken;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    final String SECRET_KEY_SIMPLE = "1234567890123456789012345678901234567890123456789012345678901234";

    JwtProvider jwtProvider = new JwtProvider(SECRET_KEY_SIMPLE, 600, 604800);

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
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE3MDc2NTk4MjEsImlhdCI6MTcwNzY2OTgyMX0.wqeuezYqSzYexc_wr50BQa8c-A_JEa9Kqn2RiFZGUfV5UT29CmBNJp-UW626Fj6O9OHjJOlCRIyNCuYZ2K62Aw"));
    }

    @Test
    @DisplayName("Testing subject extraction from token")
    void testSubjectExtraction() {
        assertEquals("1", jwtProvider.extractSubject(jwtProvider.generateSignInToken("1").getAccessToken()));
    }

}