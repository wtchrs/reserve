package reserve.signin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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

    final String SECRET_KEY_SIMPLE = "1234567890123456789012345678901234567890123456789012345678901234";

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Spy
    PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Spy
    JwtProvider jwtProvider = new JwtProvider(SECRET_KEY_SIMPLE, 600, 604800);

    SignInService signInService;

    @BeforeEach
    void setUp() {
        signInService = new SignInService(604800, userRepository, refreshTokenRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void signIn() {
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
    void refreshAccessToken() {
        RefreshToken refreshToken = new RefreshToken("refreshToken", 1L, 604800);
        Mockito.when(refreshTokenRepository.findById("refreshToken")).thenReturn(Optional.of(refreshToken));
        Mockito.doReturn(false).when(jwtProvider).isRefreshTokenExpired("refreshToken");

        SignInToken signInToken = signInService.refreshAccessToken("refreshToken");

        assertNotNull(signInToken.getAccessToken());
    }

    @Test
    void signOut() {
        Mockito.doReturn(false).when(jwtProvider).isRefreshTokenExpired("refreshToken");

        signInService.signOut("refreshToken");

        Mockito.verify(refreshTokenRepository).deleteById("refreshToken");
    }

}