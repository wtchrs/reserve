package reserve.signup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.UsernameDuplicateException;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.domain.repository.UserRepository;
import reserve.signup.dto.request.SignUpRequest;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameDuplicateException(ErrorCode.USERNAME_DUPLICATE);
        }
        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User user = new User(signUpRequest.getUsername(), hashedPassword, signUpRequest.getNickname(), "");
        userRepository.save(user);
    }

}
