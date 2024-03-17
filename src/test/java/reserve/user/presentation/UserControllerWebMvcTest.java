package reserve.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reserve.signin.infrastructure.JwtProvider;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.dto.response.UserInfoResponse;
import reserve.user.service.UserService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(JwtProvider.class)
class UserControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("Testing GET /v1/users/{username} endpoint")
    void testGetUserInfoEndpoint() throws Exception {
        UserInfoResponse value = new UserInfoResponse("username", "nickname", "description", LocalDate.of(2024, 1, 1));
        Mockito.when(userService.getUserInfo("username")).thenReturn(value);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/users/{username}", "username")
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.username").value("username"),
                jsonPath("$.nickname").value("nickname"),
                jsonPath("$.description").value("description"),
                jsonPath("$.signUpDate").value(LocalDate.of(2024, 1, 1).toString())
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/users endpoint")
    void testUpdateUserInfoEndpoint() throws Exception {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setNickname("newNickname");
        userUpdateRequest.setDescription("newDescription");

        String accessToken = jwtProvider.generateSignInToken("1").getAccessToken();

        mockMvc.perform(
                MockMvcRequestBuilders.put("/v1/users")
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        Mockito.verify(userService).update(Mockito.eq(1L), Mockito.argThat(
                request -> request.getNickname().equals("newNickname") &&
                           request.getDescription().equals("newDescription")
        ));
    }

    @Test
    @DisplayName("Testing PUT /v1/users/password endpoint")
    void testUpdatePasswordEndpoint() throws Exception {
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest();
        passwordUpdateRequest.setOldPassword("password");
        passwordUpdateRequest.setNewPassword("newPassword");
        passwordUpdateRequest.setConfirmation("newPassword");

        String accessToken = jwtProvider.generateSignInToken("1").getAccessToken();

        mockMvc.perform(
                MockMvcRequestBuilders.put("/v1/users/password")
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        Mockito.verify(userService).updatePassword(Mockito.eq(1L), Mockito.argThat(
                request -> request.getOldPassword().equals("password") &&
                           request.getNewPassword().equals("newPassword") &&
                           request.getConfirmation().equals("newPassword")
        ));
    }

    @Test
    @DisplayName("Testing DELETE /v1/users endpoint")
    void testDelete() throws Exception {
        UserDeleteRequest userDeleteRequest = new UserDeleteRequest();
        userDeleteRequest.setPassword("password");

        String accessToken = jwtProvider.generateSignInToken("1").getAccessToken();

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/v1/users")
                        .content(objectMapper.writeValueAsString(userDeleteRequest))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        Mockito.verify(userService).delete(Mockito.eq(1L), Mockito.argThat(
                request -> request.getPassword().equals("password")
        ));
    }

}
