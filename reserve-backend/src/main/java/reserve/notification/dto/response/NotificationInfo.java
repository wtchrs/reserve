package reserve.notification.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reserve.notification.domain.Notification;
import reserve.notification.domain.NotificationStatus;
import reserve.notification.domain.ResourceType;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class NotificationInfo {

    private final Long notificationId;

    private final ResourceType resourceType;

    private final Long resourceId;

    private final String message;

    private final LocalDateTime notifiedTime;

    private final NotificationStatus status;

    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
                notification.getId(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getStatus()
        );
    }

}
