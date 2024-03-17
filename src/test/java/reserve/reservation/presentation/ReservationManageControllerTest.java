package reserve.reservation.presentation;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import reserve.global.BaseRestAssuredTest;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class ReservationManageControllerTest extends BaseRestAssuredTest {

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
        jdbcTemplate.update("UPDATE reservations SET status = 'IN_SERVICE' WHERE reservation_id = ?", inService.getId());

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
    @DisplayName("[Integration] Testing POST /v1/reservations/manage/{reservationId}/cancel endpoint")
    void testCancelEndpoint() {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/cancel";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Cancel the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The reservation ID to cancel")
                        )
                ))
                .when().post(urlTemplate, ready.getId())
                .then().assertThat().statusCode(200);

        // Cancel the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The reservation ID to cancel")
                        )
                ))
                .when().post(urlTemplate, inService.getId())
                .then().assertThat().statusCode(409);

        // Cancel the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The reservation ID to cancel")
                        )
                ))
                .when().post(urlTemplate, completed.getId())
                .then().assertThat().statusCode(409);

        // Cancel the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The reservation ID to cancel")
                        )
                ))
                .when().post(urlTemplate, cancelled.getId())
                .then().assertThat().statusCode(200);
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/reservations/manage/{reservationId}/start endpoint")
    void testStartServiceEndpoint() {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/start";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Start the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to start the service")
                        )
                ))
                .when().post(urlTemplate, ready.getId())
                .then().assertThat().statusCode(200);

        // Start the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to start the service")
                        )
                ))
                .when().post(urlTemplate, inService.getId())
                .then().assertThat().statusCode(200);

        // Start the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to start the service")
                        )
                ))
                .when().post(urlTemplate, completed.getId())
                .then().assertThat().statusCode(409);

        // Start the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to start the service")
                        )
                ))
                .when().post(urlTemplate, cancelled.getId())
                .then().assertThat().statusCode(409);
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/reservations/manage/{reservationId}/complete endpoint")
    void testCompleteEndpoint() {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/complete";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        // Complete the ready reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to complete the service")
                        )
                ))
                .when().post(urlTemplate, ready.getId())
                .then().assertThat().statusCode(409);

        // Complete the in-service reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to complete the service")
                        )
                ))
                .when().post(urlTemplate, inService.getId())
                .then().assertThat().statusCode(200);

        // Complete the completed reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to complete the service")
                        )
                ))
                .when().post(urlTemplate, completed.getId())
                .then().assertThat().statusCode(200);

        // Complete the cancelled reservation
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer scheme")
                        ),
                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("The reservation ID to complete the service")
                        )
                ))
                .when().post(urlTemplate, cancelled.getId())
                .then().assertThat().statusCode(409);
    }

}
