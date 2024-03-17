package reserve.signup.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signup.dto.request.SignUpRequest;
import reserve.signup.service.SignUpService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignUpController.class)
@Import(JwtProvider.class)
class SignUpControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SignUpService signUpService;

    @Test
    @DisplayName("Testing POST /v1/sign-up endpoint")
    void testSignUpEndpoint() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("username");
        signUpRequest.setPassword("password");
        signUpRequest.setPasswordConfirmation("password");
        signUpRequest.setNickname("nickname");

        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
        ).andExpectAll(
                status().isCreated(),
                header().stringValues("Location", "/v1/users/" + signUpRequest.getUsername())
        );

        Mockito.verify(signUpService, data -> {
            SignUpRequest request = data.getAllInvocations().get(0).getArgument(0);
            assertEquals(signUpRequest.getUsername(), request.getUsername());
            assertEquals(signUpRequest.getPassword(), request.getPassword());
            assertEquals(signUpRequest.getPasswordConfirmation(), request.getPasswordConfirmation());
            assertEquals(signUpRequest.getNickname(), request.getNickname());
        }).signUp(Mockito.any());
    }

}
