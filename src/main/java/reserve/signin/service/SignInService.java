package reserve.signin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.RefreshTokenException;
import reserve.global.exception.WrongCredentialException;
import reserve.signin.domain.RefreshToken;
import reserve.signin.infrastructure.RefreshTokenRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

@Service
public class SignInService {

    private final int refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public SignInService(
            @Value("${application.security.jwt.refreshTokenExpire}") int refreshTokenExpiration,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional(readOnly = true)
    public SignInToken signIn(SignInRequest signInRequest) {
        User user = userRepository.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new WrongCredentialException(ErrorCode.WRONG_CREDENTIAL));
        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPasswordHash())) {
            throw new WrongCredentialException(ErrorCode.WRONG_CREDENTIAL);
        }
        Long userId = user.getId();
        SignInToken signInToken = jwtProvider.generateSignInToken(userId.toString());
        refreshTokenRepository.save(new RefreshToken(signInToken.getRefreshToken(), userId, refreshTokenExpiration));
        return signInToken;
    }

    public SignInToken refreshAccessToken(String refreshTokenValue) {
        if (jwtProvider.isRefreshTokenExpired(refreshTokenValue)) {
            throw new RefreshTokenException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenValue)
                .orElseThrow(() -> new RefreshTokenException(ErrorCode.EXPIRED_REFRESH_TOKEN));
        Long userId = refreshToken.getUserId();
        SignInToken signInToken = jwtProvider.generateSignInToken(userId.toString());
        // Refresh token rotation.
        refreshTokenRepository.save(new RefreshToken(signInToken.getRefreshToken(), userId, refreshTokenExpiration));
        refreshTokenRepository.delete(refreshToken);
        return signInToken;
    }

    public void signOut(String refreshTokenValue) {
        if (!jwtProvider.isRefreshTokenExpired(refreshTokenValue)) {
            refreshTokenRepository.deleteById(refreshTokenValue);
        }
    }

}
