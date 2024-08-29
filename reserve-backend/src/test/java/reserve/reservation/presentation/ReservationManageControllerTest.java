package reserve.reservation.presentation;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.request.PathParametersSnippet;
import reserve.global.BaseRestAssuredTest;
import reserve.global.exception.ErrorCode;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import javax.sql.DataSource;
import java.time.LocalDate;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static reserve.global.DocumentationSnippetUtils.*;

class ReservationManageControllerTest extends BaseRestAssuredTest {

    private static final String CANCEL_ENDPOINT_URL_TEMPLATE = "/v1/reservations/manage/{reservationId}/cancel";
    private static final String START_ENDPOINT_URL_TEMPLATE = "/v1/reservations/manage/{reservationId}/start";
    private static final String COMPLETE_ENDPOINT_URL_TEMPLATE = "/v1/reservations/manage/{reservationId}/complete";

    @Autowired
    DataSource dataSource;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    NotificationRepository notificationRepository;

    User registrant;
    Reservation ready, inService, completed, cancelled;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        User user = userRepository.save(new User("username", "password", "nickname", "description"));
        registrant = userRepository.save(new User("registrant", "password", "nickname", "description"));
        Store store = storeRepository.save(new Store(registrant, "store", "address", "description"));

        ready = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));

        inService = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        jdbcTemplate.update(
                "UPDATE reservations SET status = 'IN_SERVICE' WHERE reservation_id = ?",
                inService.getId()
        );

        completed = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        jdbcTemplate.update("UPDATE reservations SET status = 'COMPLETED' WHERE reservation_id = ?", completed.getId());

        cancelled = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        jdbcTemplate.update("UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?", cancelled.getId());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        reservationRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[Integration] Testing POST " + CANCEL_ENDPOINT_URL_TEMPLATE + " endpoint for ready reservation")
    void testCancelEndpointForReadyReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Cancel the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCancelPathParametersSnippet()
                ))
                .when().post(CANCEL_ENDPOINT_URL_TEMPLATE, ready.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + CANCEL_ENDPOINT_URL_TEMPLATE + " endpoint for in-service reservation"
    )
    void testCancelEndpointForInServiceReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Cancel the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCancelPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(CANCEL_ENDPOINT_URL_TEMPLATE, inService.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_CANCEL.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_CANCEL.getMessage()));
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + CANCEL_ENDPOINT_URL_TEMPLATE + " endpoint for completed reservation"
    )
    void testCancelEndpointForCompletedReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Cancel the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCancelPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(CANCEL_ENDPOINT_URL_TEMPLATE, completed.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_CANCEL.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_CANCEL.getMessage()));
    }

    @Test
    @DisplayName("[Integration] Testing POST " + CANCEL_ENDPOINT_URL_TEMPLATE + " endpoint for cancelled reservation")
    void testCancelEndpointForCancelledReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Cancel the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCancelPathParametersSnippet()
                ))
                .when().post(CANCEL_ENDPOINT_URL_TEMPLATE, cancelled.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName("[Integration] Testing POST " + START_ENDPOINT_URL_TEMPLATE + " endpoint for ready reservation")
    void testStartServiceEndpointForReadyReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Start the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToStartPathParametersSnippet()
                ))
                .when().post(START_ENDPOINT_URL_TEMPLATE, ready.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName("[Integration] Testing POST " + START_ENDPOINT_URL_TEMPLATE + " endpoint for in-service reservation")
    void testStartServiceEndpointForInServiceReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Start the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToStartPathParametersSnippet()
                ))
                .when().post(START_ENDPOINT_URL_TEMPLATE, inService.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + START_ENDPOINT_URL_TEMPLATE + " endpoint for completed reservation"
    )
    void testStartServiceEndpointForCompletedReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Start the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToStartPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(START_ENDPOINT_URL_TEMPLATE, completed.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_START.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_START.getMessage()));
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + START_ENDPOINT_URL_TEMPLATE + " endpoint for cancelled reservation"
    )
    void testStartServiceEndpointForCancelledReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Start the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToStartPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(START_ENDPOINT_URL_TEMPLATE, cancelled.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_START.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_START.getMessage()));
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + COMPLETE_ENDPOINT_URL_TEMPLATE + " endpoint for ready reservation"
    )
    void testCompleteEndpointForReadyReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Complete the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCompletePathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(COMPLETE_ENDPOINT_URL_TEMPLATE, ready.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_COMPLETE.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_COMPLETE.getMessage()));
    }

    @Test
    @DisplayName(
            "[Integration] Testing POST " + COMPLETE_ENDPOINT_URL_TEMPLATE + " endpoint for in-service reservation"
    )
    void testCompleteEndpointForInServiceReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Complete the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCompletePathParametersSnippet()
                ))
                .when().post(COMPLETE_ENDPOINT_URL_TEMPLATE, inService.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName("[Integration] Testing POST " + COMPLETE_ENDPOINT_URL_TEMPLATE + " endpoint for completed reservation")
    void testCompleteEndpointForCompletedReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Complete the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCompletePathParametersSnippet()
                ))
                .when().post(COMPLETE_ENDPOINT_URL_TEMPLATE, completed.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName(
            "[Integration][Fail] Testing POST " + COMPLETE_ENDPOINT_URL_TEMPLATE + " endpoint for cancelled reservation"
    )
    void testCompleteEndpointForCancelledReservation() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Complete the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdToCompletePathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().post(COMPLETE_ENDPOINT_URL_TEMPLATE, cancelled.getId())
                .then()
                .statusCode(409)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_CANNOT_COMPLETE.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_CANNOT_COMPLETE.getMessage()));
    }

    private static PathParametersSnippet reservationIdToCancelPathParametersSnippet() {
        return pathParameters(parameterWithName("reservationId").description("The reservation ID to cancel"));
    }

    private static PathParametersSnippet reservationIdToStartPathParametersSnippet() {
        return pathParameters(parameterWithName("reservationId").description("The reservation ID to start"));
    }

    private static PathParametersSnippet reservationIdToCompletePathParametersSnippet() {
        return pathParameters(parameterWithName("reservationId").description("The reservation ID to complete"));
    }

}
