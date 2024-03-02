package reserve.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class UserControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    String accessToken;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        User user = userRepository.save(new User(
                "username",
                passwordEncoder.encode("password"),
                "nickname",
                "description"
        ));
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));
        accessToken = signInToken.getAccessToken();
    }

    @Test
    @DisplayName("Testing GET /v1/users/{username} endpoint")
    void testGetUserInfoEndpoint() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/users/{username}", "username")
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.username").value("username"),
                jsonPath("$.nickname").value("nickname"),
                jsonPath("$.description").value("description"),
                jsonPath("$.signUpDate").exists()
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/users endpoint")
    void testUpdateUserInfoEndpoint() throws Exception {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setNickname("newNickname");
        userUpdateRequest.setDescription("newDescription");

        mockMvc.perform(
                MockMvcRequestBuilders.put("/v1/users")
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        userRepository.findByUsername("username").ifPresentOrElse(
                user -> {
                    assertEquals("newNickname", user.getNickname());
                    assertEquals("newDescription", user.getDescription());
                },
                () -> fail("User not found")
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/users/password endpoint")
    void testUpdatePasswordEndpoint() throws Exception {
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest();
        passwordUpdateRequest.setOldPassword("password");
        passwordUpdateRequest.setNewPassword("newPassword");
        passwordUpdateRequest.setConfirmation("newPassword");

        mockMvc.perform(
                MockMvcRequestBuilders.put("/v1/users/password")
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        userRepository.findByUsername("username").ifPresentOrElse(
                user -> assertTrue(passwordEncoder.matches("newPassword", user.getPasswordHash())),
                () -> fail("User not found")
        );
    }

    @Test
    @DisplayName("Testing DELETE /v1/users endpoint")
    void testDelete() throws Exception {
        UserDeleteRequest userDeleteRequest = new UserDeleteRequest();
        userDeleteRequest.setPassword("password");

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/v1/users")
                        .content(objectMapper.writeValueAsString(userDeleteRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        userRepository.findByUsername("username").ifPresent(user -> fail("User not deleted"));
    }

}
