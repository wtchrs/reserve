package reserve.signin.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.cookies.ResponseCookiesSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.BaseRestAssuredTest;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.infrastructure.JwtProvider;
import reserve.signin.infrastructure.RefreshTokenRepository;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.concurrent.TimeUnit;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static reserve.global.DocumentationSnippetUtils.bearerTokenAuthorizationSnippet;

class SignInControllerTest extends BaseRestAssuredTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    User user;

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        user = userRepository.save(new User(
                "username",
                passwordEncoder.encode("password"),
                "nickname",
                "description"
        ));
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/sign-in endpoint")
    void testSignInEndpoint() throws JsonProcessingException {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername(user.getUsername());
        signInRequest.setPassword("password");

        String payload = objectMapper.writeValueAsString(signInRequest);

        Response response = RestAssured
                .given(spec).contentType(MediaType.APPLICATION_JSON_VALUE).body(payload)
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        signInRequestFieldsSnippet(),
                        accessTokenResponseHeadersSnippet(),
                        refreshTokenResponseCookiesSnippet()
                ))
                .relaxedHTTPSValidation()
                .when().post("/v1/sign-in");

        response.then().statusCode(200);

        assertFalse(jwtProvider.isAccessTokenExpired(response.getHeader("Authorization")));
        assertFalse(jwtProvider.isRefreshTokenExpired(response.getCookie("refresh")));

        refreshTokenRepository.findById(response.getCookie("refresh")).ifPresentOrElse(
                refreshToken -> assertEquals(user.getId(), refreshToken.getUserId()),
                () -> fail("Refresh token not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/token-refresh endpoint")
    void testRefreshAccessTokenEndpoint() throws JsonProcessingException {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        String payload = objectMapper.writeValueAsString(signInRequest);

        String refreshToken = RestAssured
                .given(spec).contentType(MediaType.APPLICATION_JSON_VALUE).body(payload)
                .relaxedHTTPSValidation()
                .when().post("/v1/sign-in")
                .getCookie("refresh");

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Response response = RestAssured
                .given(spec).cookie("refresh", refreshToken)
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        accessTokenResponseHeadersSnippet(),
                        refreshTokenResponseCookiesSnippet()
                ))
                .relaxedHTTPSValidation()
                .when().post("/v1/token-refresh");

        response.then().statusCode(200);

        assertFalse(jwtProvider.isAccessTokenExpired(response.getHeader("Authorization")));
        assertFalse(jwtProvider.isRefreshTokenExpired(response.getCookie("refresh")));
        assertNotEquals(refreshToken, response.getCookie("refresh"));

        refreshTokenRepository.findById(refreshToken).ifPresent(ignored1 -> fail("Old refresh token not deleted"));
        refreshTokenRepository.findById(response.getCookie("refresh")).ifPresentOrElse(
                refreshToken1 -> assertEquals(user.getId(), refreshToken1.getUserId()),
                () -> fail("New refresh token not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/sign-out endpoint")
    void testSignOutEndpoint() throws JsonProcessingException {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        String payload = objectMapper.writeValueAsString(signInRequest);

        Response response = RestAssured
                .given(spec).contentType(MediaType.APPLICATION_JSON_VALUE).body(payload)
                .relaxedHTTPSValidation()
                .when().post("/v1/sign-in");

        response.then().statusCode(200);

        String accessToken = response.getHeader("Authorization");
        String refreshToken = response.getCookie("refresh");

        Response response1 = RestAssured
                .given(spec).header("Authorization", "Bearer " + accessToken).cookie("refresh", refreshToken)
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        expiringRefreshTokenResponseCookiesSnippet()
                ))
                .relaxedHTTPSValidation()
                .when().post("/v1/sign-out");

        response1.then().statusCode(200).cookie("refresh", "");
        assertEquals(0, response1.getDetailedCookie("refresh").getMaxAge());

        refreshTokenRepository.findById(refreshToken).ifPresent(refreshToken1 -> fail("Refresh token not deleted"));
    }

    /**
     * @return The request fields snippet
     * @see SignInRequest
     */
    private static RequestFieldsSnippet signInRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("username").description("The username of the user"),
                fieldWithPath("password").description("The password of the user")
        );
    }

    private static ResponseHeadersSnippet accessTokenResponseHeadersSnippet() {
        return responseHeaders(headerWithName("Authorization").description("The access token"));
    }

    private static ResponseCookiesSnippet refreshTokenResponseCookiesSnippet() {
        return responseCookies(cookieWithName("refresh").description("The refresh token"));
    }

    private static ResponseCookiesSnippet expiringRefreshTokenResponseCookiesSnippet() {
        return responseCookies(
                cookieWithName("refresh").description("The refresh token cookie. It is expired immediately.")
        );
    }

}
