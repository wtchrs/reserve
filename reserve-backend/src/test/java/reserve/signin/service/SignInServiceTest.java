package reserve.signin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.global.TestUtils;
import reserve.signin.domain.RefreshToken;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signin.infrastructure.RefreshTokenRepository;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.signup.infrastructure.Pbkdf2PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    final String ACCESS_TOKEN_SECRET = "1234567890123456789012345678901234567890123456789012345678901234";
    final String REFRESH_TOKEN_SECRET = "9876543210987654321098765432109876543210987654321098765432109876";

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Spy
    PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Spy
    JwtProvider jwtProvider = new JwtProvider(ACCESS_TOKEN_SECRET, REFRESH_TOKEN_SECRET, 600, 604800);

    SignInService signInService;

    @BeforeEach
    void setUp() {
        signInService = new SignInService(604800, userRepository, refreshTokenRepository, passwordEncoder, jwtProvider);
    }

    @Test
    @DisplayName("Testing sign-in functionality")
    void testSignIn() {
        User user = new User("username", passwordEncoder.encode("password"), "nickname", "description");
        user = Mockito.spy(user);
        Mockito.when(user.getId()).thenReturn(1L);
        Mockito.when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        SignInRequest signInRequest = Mockito.spy(new SignInRequest());
        Mockito.when(signInRequest.getUsername()).thenReturn("username");
        Mockito.when(signInRequest.getPassword()).thenReturn("password");
        SignInToken signInToken = signInService.signIn(signInRequest);

        assertNotNull(signInToken.getAccessToken());
        Mockito.verify(refreshTokenRepository).save(Mockito.argThat(
                refreshToken -> refreshToken.getTokenValue().equals(signInToken.getRefreshToken()) &&
                                refreshToken.getUserId().equals(1L) &&
                                refreshToken.getExpiration() == 604800
        ));
    }

    @Test
    @DisplayName("Testing access token refresh functionality")
    void testRefreshAccessToken() {
        String refreshTokenString = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(1L)).getRefreshToken();
        RefreshToken refreshToken = new RefreshToken(refreshTokenString, 1L, 604800);
        Mockito.when(refreshTokenRepository.findById(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        SignInToken signInToken = signInService.refreshAccessToken(refreshTokenString);

        assertNotNull(signInToken.getAccessToken());
    }

    @Test
    @DisplayName("Testing sign-out functionality")
    void testSignOut() {
        Mockito.doReturn(false).when(jwtProvider).isRefreshTokenExpired("refreshToken");

        signInService.signOut("refreshToken");

        Mockito.verify(refreshTokenRepository).deleteById("refreshToken");
    }

}
