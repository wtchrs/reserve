package reserve.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reserve.notification.domain.Notification;
import reserve.notification.domain.ResourceType;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    NotificationService notificationService;

    @Test
    @DisplayName("Testing creation of notification for reservation")
    void testCreationOfNotificationForReservation() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(reservationRepository.existsById(1L)).thenReturn(true);
        Mockito.when(reservationRepository.findStoreUserIdByIdIncludeDeleted(1L)).thenReturn(Optional.of(1L));

        notificationService.notifyReservation(1L, 1L, "message for user", "message for store registrant");

        Mockito.verify(reservationRepository).findStoreUserIdByIdIncludeDeleted(1L);
        Mockito.verify(notificationRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    @DisplayName("Testing user notification retrieval")
    void testUserNotificationRetrieval() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = Mockito.mock(User.class);

        Notification notification1 = Mockito.spy(new Notification(user, ResourceType.RESERVATION, 1L, "message1"));
        Mockito.when(notification1.getId()).thenReturn(1L);
        Notification notification2 = Mockito.spy(new Notification(user, ResourceType.RESERVATION, 1L, "message2"));
        Mockito.when(notification2.getId()).thenReturn(2L);
        Notification notification3 = Mockito.spy(new Notification(user, ResourceType.RESERVATION, 1L, "message3"));
        Mockito.when(notification3.getId()).thenReturn(3L);

        Mockito.when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(notification1, notification2, notification3), pageable, 3L));

        NotificationInfoListResponse response = notificationService.getUserNotifications(1L, pageable);

        assertEquals(3, response.getCount());
        assertEquals(3, response.getResults().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertThat(response.getResults()).extracting("notificationId").contains(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Testing marking a specific notification as read")
    void testMarkingNotificationAsRead() {
        notificationService.readNotification(1L, 1L);

        Mockito.verify(notificationRepository).setReadByUserIdAndId(1L, 1L);
    }

    @Test
    @DisplayName("Testing marking all user notifications as read")
    void testMarkingAllUserNotificationAsRead() {
        notificationService.readAllNotifications(1L);

        Mockito.verify(notificationRepository).setReadAllByUserId(1L);
    }

}