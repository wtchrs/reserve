package reserve.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.notification.domain.Notification;
import reserve.notification.domain.ResourceType;
import reserve.notification.dto.response.NotificationInfo;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void notifyReservation(Long userId, Long reservationId, String message, String registrantMessage) {
        if (!userRepository.existsById(userId)) {
            throw new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO);
        }
        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        notificationRepository.save(new Notification(
                userRepository.getReferenceById(userId),
                ResourceType.RESERVATION,
                reservationId,
                message
        ));
        reservationRepository.findStoreUserIdByIdIncludeDeleted(reservationId).ifPresent(
                storeRegistrantId -> notificationRepository.save(new Notification(
                        userRepository.getReferenceById(storeRegistrantId),
                        ResourceType.RESERVATION,
                        reservationId,
                        registrantMessage
                ))
        );
    }

    @Transactional
    public void notifyReservation(Long userId, Long reservationId, String message) {
        notifyReservation(userId, reservationId, message, message);
    }

    @Transactional(readOnly = true)
    public NotificationInfoListResponse getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notificationPage =
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<NotificationInfo> dtoPage = notificationPage.map(NotificationInfo::from);
        return NotificationInfoListResponse.from(dtoPage);
    }

    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        notificationRepository.setReadByUserIdAndId(userId, notificationId);
    }

    @Transactional
    public void readAllNotifications(Long userId) {
        notificationRepository.setReadAllByUserId(userId);
    }

}
