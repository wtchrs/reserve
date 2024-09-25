package reserve.notification.representation;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reserve.global.BaseRestAssuredTest;
import reserve.global.TestUtils;
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

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
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .when().get("/v1/notifications")
                .then()
                .statusCode(200)
                .body("count", equalTo(3))
                .body("pageSize", equalTo(20))
                .body("pageNumber", equalTo(0))
                .body("hasNext", equalTo(false))
                .body("results[2].notificationId", equalTo(notification1.getId().intValue()))
                .body("results[2].message", equalTo("message1"))
                .body("results[1].notificationId", equalTo(notification2.getId().intValue()))
                .body("results[1].message", equalTo("message2"))
                .body("results[0].notificationId", equalTo(notification3.getId().intValue()))
                .body("results[0].message", equalTo("message3"));
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/notifications/{notificationId}/read endpoint")
    void testReadNotificationEndpoint() {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
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
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
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
