package reserve.notification.representation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.service.NotificationService;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationInfoListResponse getUserNotifications(@Authentication AuthInfo authInfo, Pageable pageable) {
        return notificationService.getUserNotifications(authInfo.getUserId(), pageable);
    }

    @PostMapping("/{notificationId}/read")
    public void readNotification(
            @Authentication AuthInfo authInfo,
            @PathVariable("notificationId") Long notificationId
    ) {
        notificationService.readNotification(authInfo.getUserId(), notificationId);
    }

    @PostMapping("/read-all")
    public void readAllNotifications(@Authentication AuthInfo authInfo) {
        notificationService.readAllNotifications(authInfo.getUserId());
    }

}
