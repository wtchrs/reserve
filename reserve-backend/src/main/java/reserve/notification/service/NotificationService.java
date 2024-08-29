package reserve.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.notification.domain.Notification;
import reserve.notification.domain.ResourceType;
import reserve.notification.dto.response.NotificationInfo;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.dto.ReservationForNotifyDto;
import reserve.reservation.infrastructure.ReservationQueryRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    @Transactional
    public void notifyReservation(Long reservationId, String message, String registrantMessage) {
        ReservationForNotifyDto reservationForNotifyDto = reservationQueryRepository.findForNotifyById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        notificationRepository.save(new Notification(
                userRepository.getReferenceById(reservationForNotifyDto.getUserId()),
                ResourceType.RESERVATION,
                reservationId,
                message
        ));
        notificationRepository.save(new Notification(
                userRepository.getReferenceById(reservationForNotifyDto.getRegistrantId()),
                ResourceType.RESERVATION,
                reservationId,
                registrantMessage
        ));
    }

    @Transactional
    public void notifyReservation(Long reservationId, String message) {
        notifyReservation(reservationId, message, message);
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
