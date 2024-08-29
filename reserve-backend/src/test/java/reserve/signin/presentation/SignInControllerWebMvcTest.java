package reserve.signin.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signin.service.SignInService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignInController.class)
@Import(JwtProvider.class)
class SignInControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SignInService signInService;

    @Test
    @DisplayName("Testing POST /v1/sign-in endpoint")
    void testSignInEndpoint() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        SignInToken signInToken = new SignInToken("access", "refresh");
        Mockito.when(signInService.signIn(Mockito.any())).thenReturn(signInToken);

        mockMvc.perform(
                post("/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest))
        ).andExpectAll(
                status().isOk(),
                header().string("Authorization", signInToken.getAccessToken()),
                cookie().value("refresh", signInToken.getRefreshToken())
        );
    }

    @Test
    @DisplayName("Testing POST /v1/token-refresh endpoint")
    void testRefreshAccessTokenEndpoint() throws Exception {
        String refreshTokenValue = "refreshToken";
        SignInToken signInToken = new SignInToken("newAccessToken", "newRefreshToken");
        Cookie refreshCookie = new Cookie("refresh", refreshTokenValue);

        Mockito.when(signInService.refreshAccessToken(refreshTokenValue)).thenReturn(signInToken);

        mockMvc.perform(
                post("/v1/token-refresh").cookie(refreshCookie)
        ).andExpectAll(
                status().isOk(),
                header().string("Authorization", signInToken.getAccessToken()),
                cookie().value("refresh", signInToken.getRefreshToken())
        );
    }

    @Test
    @DisplayName("Testing POST /v1/sign-out endpoint")
    void testSignOutEndpoint() throws Exception {
        String refreshTokenValue = "refreshToken";
        Cookie refreshCookie = new Cookie("refresh", refreshTokenValue);

        mockMvc.perform(
                post("/v1/sign-out").cookie(refreshCookie)
        ).andExpectAll(
                status().isOk(),
                cookie().maxAge("refresh", 0)
        );
    }

}
