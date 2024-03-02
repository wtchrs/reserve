package reserve.notification.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class NotificationControllerTest {

    @PersistenceContext
    EntityManager em;

    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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

    User user, registrant;
    Store store;
    Reservation reservation;
    Notification notification1, notification2, notification3;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        user = userRepository.save(new User("user", "password", "nickname", "description"));
        registrant = userRepository.save(new User("registrant", "password", "nickname", "description"));
        store = storeRepository.save(new Store(registrant, "store", 1000, "address", "description"));
        reservation = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));

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

    @Test
    @DisplayName("Testing GET /v1/notifications endpoint")
    void testGetUserNotificationsEndpoint() throws Exception {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(get("/v1/notifications").header("Authorization", "Bearer " + signInToken.getAccessToken()))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.count").value(3),
                        jsonPath("$.results[2].message").value("message1"),
                        jsonPath("$.results[1].message").value("message2"),
                        jsonPath("$.results[0].message").value("message3")
                );
    }

    @Test
    @DisplayName("Testing POST /v1/notifications/{notificationId}/read endpoint")
    void testReadNotificationEndpoint() throws Exception {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                        post("/v1/notifications/{notificationId}/read", notification1.getId())
                                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                )
                .andExpect(status().isOk());

        em.flush();
        em.clear();

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
    @DisplayName("Testing POST /v1/notifications/read-all endpoint")
    void testReadAllNotificationsEndpoint() throws Exception {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                        post("/v1/notifications/read-all")
                                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                )
                .andExpect(status().isOk());

        em.flush();
        em.clear();

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
