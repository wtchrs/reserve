package reserve.notification.representation;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reserve.global.BaseRestAssuredTest;
import reserve.notification.domain.Notification;
import reserve.notification.domain.ResourceType;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class NotificationControllerTest extends BaseRestAssuredTest {

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

    User user;
    Notification notification1, notification2, notification3;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("user", "password", "nickname", "description"));
        User registrant = userRepository.save(new User("registrant", "password", "nickname", "description"));
        Store store = storeRepository.save(new Store(registrant, "store", "address", "description"));
        Reservation reservation = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));

        notification1 = notificationRepository.save(new Notification(
                user,
                ResourceType.RESERVATION,
                reservation.getId(),
                "message1"
        ));
        notification2 = notificationRepository.save(new Notification(
                user,
                ResourceType.RESERVATION,
                reservation.getId(),
                "message2"
        ));
        notification3 = notificationRepository.save(new Notification(
                user,
                ResourceType.RESERVATION,
                reservation.getId(),
                "message3"
        ));
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        reservationRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/notifications endpoint")
    void testGetUserNotificationsEndpoint() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(headerWithName("Authorization").description("Access token in bearer scheme")),
                        responseFields(
                                fieldWithPath("count").description("The number of notifications"),
                                fieldWithPath("pageSize").description("The number of notifications per page"),
                                fieldWithPath("pageNumber").description("The current page number. It starts from 0"),
                                fieldWithPath("hasNext").description("The existence of the next page"),
                                fieldWithPath("results[].notificationId").description("The ID of the notification"),
                                fieldWithPath("results[].resourceType").description("The type of the resource"),
                                fieldWithPath("results[].resourceId").description("The ID of the resource"),
                                fieldWithPath("results[].message").description("The message of the notification"),
                                fieldWithPath("results[].status").description("The status of the notification"),
                                fieldWithPath("results[].notifiedTime")
                                        .description("The time when the notification was notified")
                        )
                ))
                .when().get("/v1/notifications")
                .then()
                .statusCode(200)
                .body(
                        "count", equalTo(3),
                        "results[2].message", equalTo("message1"),
                        "results[1].message", equalTo("message2"),
                        "results[0].message", equalTo("message3")
                );
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/notifications/{notificationId}/read endpoint")
    void testReadNotificationEndpoint() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(headerWithName("Authorization").description("Access token in bearer scheme")),
                        pathParameters(parameterWithName("notificationId").description("The ID of the notification"))
                ))
                .when().post("/v1/notifications/{notificationId}/read", notification1.getId())
                .then()
                .statusCode(200);

        notificationRepository.findById(notification1.getId()).ifPresentOrElse(
                notification -> assertTrue(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
        notificationRepository.findById(notification2.getId()).ifPresentOrElse(
                notification -> assertFalse(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
        notificationRepository.findById(notification3.getId()).ifPresentOrElse(
                notification -> assertFalse(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/notifications/read-all endpoint")
    void testReadAllNotificationsEndpoint() {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(headerWithName("Authorization").description("Access token in bearer scheme"))
                ))
                .when().post("/v1/notifications/read-all")
                .then()
                .statusCode(200);

        notificationRepository.findById(notification1.getId()).ifPresentOrElse(
                notification -> assertTrue(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
        notificationRepository.findById(notification2.getId()).ifPresentOrElse(
                notification -> assertTrue(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
        notificationRepository.findById(notification3.getId()).ifPresentOrElse(
                notification -> assertTrue(notification.isStatusRead()),
                () -> fail("Notification not found")
        );
    }

}
