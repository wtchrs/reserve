package reserve.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.signup.infrastructure.Pbkdf2PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.dto.response.UserInfoResponse;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void getUserInfo() {
        User user = new User("username", "passwordHash", "nickname", "description");
        user = Mockito.spy(user);
        Mockito.when(user.getCreatedAt()).thenReturn(LocalDateTime.now());
        Mockito.when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        UserInfoResponse response = userService.getUserInfo("username");

        assertEquals(response.getUsername(), "username");
        assertEquals(response.getNickname(), "nickname");
        assertEquals(response.getDescription(), "description");
    }

    @Test
    void update() {
        User user = new User("username", "passwordHash", "nickname", "description");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserUpdateRequest userUpdateRequest = Mockito.spy(new UserUpdateRequest());
        Mockito.when(userUpdateRequest.getNickname()).thenReturn("newNickname");
        Mockito.when(userUpdateRequest.getDescription()).thenReturn("newDescription");
        userService.update(1L, userUpdateRequest);

        assertEquals(user.getNickname(), "newNickname");
        assertEquals(user.getDescription(), "newDescription");
    }

    @Test
    void updatePassword() {
        User user = new User("username", passwordEncoder.encode("password"), "nickname", "description");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PasswordUpdateRequest passwordUpdateRequest = Mockito.spy(new PasswordUpdateRequest());
        Mockito.when(passwordUpdateRequest.getOldPassword()).thenReturn("password");
        Mockito.when(passwordUpdateRequest.getNewPassword()).thenReturn("newPassword");
        userService.updatePassword(1L, passwordUpdateRequest);

        assertTrue(passwordEncoder.matches("newPassword", user.getPasswordHash()));
    }

    @Test
    void delete() {
        User user = new User("username", passwordEncoder.encode("password"), "nickname", "description");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDeleteRequest userDeleteRequest = Mockito.spy(new UserDeleteRequest());
        Mockito.when(userDeleteRequest.getPassword()).thenReturn("password");
        userService.delete(1L, userDeleteRequest);
    }

}