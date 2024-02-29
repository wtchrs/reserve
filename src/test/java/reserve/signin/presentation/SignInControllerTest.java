package reserve.signin.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.dto.response.SignInResponse;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signin.infrastructure.RefreshTokenRepository;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class SignInControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    Long userId;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        User user = userRepository.save(new User(
                "username",
                passwordEncoder.encode("password"),
                "nickname",
                "description"
        ));
        userId = user.getId();
    }

    @Test
    @DisplayName("Testing POST /v1/sign-in endpoint")
    void testSignInEndpoint() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        ResultActions resultActions = mockMvc.perform(
                post("/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest))
        );

        resultActions
                .andExpect(status().isOk())
                .andExpectAll(
                        cookie().exists("refresh"),
                        jsonPath("$.accessToken").isString()
                );

        String refresh = resultActions.andReturn().getResponse().getCookie("refresh").getValue();
        refreshTokenRepository.findById(refresh).ifPresentOrElse(
                refreshToken -> assertEquals(userId, refreshToken.getUserId()),
                () -> fail("Refresh token not found")
        );
    }

    @Test
    @DisplayName("Testing POST /v1/token-refresh endpoint")
    void testRefreshAccessTokenEndpoint() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        Cookie refreshCookie = mockMvc.perform(
                        post("/v1/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signInRequest))
                )
                .andReturn().getResponse().getCookie("refresh");

        ResultActions resultActions;

        try (var ignored = Mockito.mockConstruction(
                Date.class,
                (mock, context) -> Mockito.when(mock.getTime()).thenReturn(new Date().getTime() + 600 * 1000)
        )) {
            resultActions = mockMvc.perform(post("/v1/token-refresh").cookie(refreshCookie));
        }

        resultActions
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh"))
                .andExpect(jsonPath("$.accessToken").isString());

        String newRefresh = resultActions.andReturn().getResponse().getCookie("refresh").getValue();
        assertNotEquals(refreshCookie.getValue(), newRefresh);

        refreshTokenRepository.findById(refreshCookie.getValue())
                .ifPresent(refreshToken -> fail("Old refresh token not deleted"));
        refreshTokenRepository.findById(newRefresh).ifPresentOrElse(
                refreshToken -> assertEquals(userId, refreshToken.getUserId()),
                () -> fail("New refresh token not found")
        );
    }

    @Test
    @DisplayName("Testing POST /v1/sign-out endpoint")
    void testSignOutEndpoint() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        ResultActions resultActions = mockMvc.perform(
                post("/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest))
        );

        String content = resultActions.andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readValue(content, SignInResponse.class).getAccessToken();
        Cookie refresh = resultActions.andReturn().getResponse().getCookie("refresh");

        mockMvc.perform(post("/v1/sign-out")
                                .header("Authorization", "Bearer " + accessToken)
                                .cookie(refresh))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refresh", 0));

        refreshTokenRepository.findById(refresh.getValue())
                .ifPresent(refreshToken -> fail("Refresh token not deleted"));
    }

}
