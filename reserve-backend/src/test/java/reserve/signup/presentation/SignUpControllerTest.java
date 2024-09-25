package reserve.signup.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import reserve.global.BaseRestAssuredTest;
import reserve.signup.dto.request.SignUpRequest;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

class SignUpControllerTest extends BaseRestAssuredTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Transactional
    @Commit
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/sign-up endpoint")
    void signUp() throws JsonProcessingException {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("username");
        signUpRequest.setPassword("password");
        signUpRequest.setPasswordConfirmation("password");
        signUpRequest.setNickname("nickname");

        String payload = objectMapper.writeValueAsString(signUpRequest);

        RestAssured
                .given(spec).body(payload).contentType("application/json")
                .relaxedHTTPSValidation()
                .when().post("/v1/sign-up")
                .then().statusCode(201).header("Location", "/v1/users/username");

        userRepository.findByUsername("username").ifPresentOrElse(
                user -> {
                    assertEquals("username", user.getUsername());
                    assertTrue(passwordEncoder.matches("password", user.getPasswordHash()));
                    assertEquals("nickname", user.getNickname());
                },
                () -> fail("User not found")
        );
    }

}