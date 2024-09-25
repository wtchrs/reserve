package reserve.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reserve.notification.domain.Notification;
import reserve.notification.domain.NotificationStatus;
import reserve.notification.domain.ResourceType;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class NotificationInfo {

    @Schema(description = "ID of notification", example = "1")
    private final Long notificationId;

    @Schema(description = "Type of resource", example = "RESERVATION")
    private final ResourceType resourceType;

    @Schema(description = "ID of resource", example = "1")
    private final Long resourceId;

    @Schema(description = "Message of notification", example = "Reservation is canceled")
    private final String message;

    @Schema(description = "Time of notification", example = "2021-07-01T00:00:00")
    private final LocalDateTime notifiedTime;

    @Schema(description = "Status of notification (READ, UNREAD)",
            example = "READ")
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
