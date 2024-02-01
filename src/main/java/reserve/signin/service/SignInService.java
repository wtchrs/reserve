package reserve.signin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.WrongCredentialException;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class SignInService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public SignInToken signIn(SignInRequest signInRequest) {
        User user = userRepository.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new WrongCredentialException(ErrorCode.WRONG_CREDENTIAL));
        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPasswordHash())) {
            throw new WrongCredentialException(ErrorCode.WRONG_CREDENTIAL);
        }
        // TODO: Save refresh token in the token repository.
        return jwtProvider.generateSignInToken(user.getId().toString());
    }

    public SignInToken refreshAccessToken(String refreshToken) {
        // TODO
        throw new UnsupportedOperationException("Not implemented.");
    }

}
