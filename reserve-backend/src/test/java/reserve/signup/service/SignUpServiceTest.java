package reserve.signup.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.signup.dto.request.SignUpRequest;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.signup.infrastructure.Pbkdf2PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Spy
    PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SignUpService signUpService;

    @Test
    @DisplayName("Testing user sign up")
    void testSignUp() {
        SignUpRequest signUpRequest = Mockito.spy(new SignUpRequest());
        Mockito.when(signUpRequest.getUsername()).thenReturn("username");
        Mockito.when(signUpRequest.getPassword()).thenReturn("password");
        Mockito.when(signUpRequest.getNickname()).thenReturn("nickname");
        Mockito.when(userRepository.existsByUsername("username")).thenReturn(false);

        signUpService.signUp(signUpRequest);

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

}