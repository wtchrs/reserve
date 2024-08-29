package reserve.notification.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import reserve.notification.domain.Notification;
import reserve.notification.domain.ResourceType;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NotificationRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("Testing retrieval of user's notifications list")
    void testUserNotificationsListRetrieval() {
        User user = userRepository.save(new User("user1", "password", "hello", "description"));
        Notification notification1 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message1"));
        Notification notification2 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message2"));
        Notification notification3 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message3"));
        Pageable pageable = PageRequest.of(0, 20);

        Page<Notification> resultPage =
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        assertEquals(3, resultPage.getTotalElements());
        assertEquals(notification3, resultPage.getContent().get(0));
        assertEquals(notification2, resultPage.getContent().get(1));
        assertEquals(notification1, resultPage.getContent().get(2));
    }

    @Test
    @DisplayName("Testing marking a specific notification as read")
    void testMarkingNotificationAsRead() {
        User user = userRepository.save(new User("user1", "password", "hello", "description"));
        Notification notification =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message"));

        assertFalse(notification.isStatusRead());
        notificationRepository.setReadByUserIdAndId(user.getId(), notification.getId());

        em.refresh(notification);

        assertTrue(notificationRepository.findById(notification.getId()).get().isStatusRead());
    }

    @Test
    @DisplayName("Testing marking all notifications of a user as read")
    void testMarkingAllUserNotificationsAsRead() {
        User user = userRepository.save(new User("user1", "password", "hello", "description"));
        Notification notification1 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message1"));
        Notification notification2 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message2"));
        Notification notification3 =
                notificationRepository.save(new Notification(user, ResourceType.RESERVATION, 1L, "message3"));

        notificationRepository.setReadAllByUserId(user.getId());

        em.refresh(notification1);
        em.refresh(notification2);
        em.refresh(notification3);

        notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 20))
                .forEach(notification -> assertTrue(notification.isStatusRead()));
    }

}
