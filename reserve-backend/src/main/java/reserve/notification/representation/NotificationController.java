package reserve.notification.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.notification.dto.response.NotificationInfoListResponse;
import reserve.notification.service.NotificationService;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "Get user notifications",
            description = "Get sign-in user's notifications",
            operationId = "1_getUserNotifications"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with user notifications",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationInfoListResponse.class)
            )
    ))
    public NotificationInfoListResponse getUserNotifications(
            @Authentication AuthInfo authInfo,
            @ParameterObject Pageable pageable
    ) {
        return notificationService.getUserNotifications(authInfo.getUserId(), pageable);
    }

    @PostMapping("/{notificationId}/read")
    @Operation(
            summary = "Read notification",
            description = "Mark notification as read by notification ID",
            operationId = "2_readNotification"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully read"))
    public void readNotification(
            @Authentication AuthInfo authInfo,
            @PathVariable("notificationId") @Schema(description = "Notification ID", example = "1") Long notificationId
    ) {
        notificationService.readNotification(authInfo.getUserId(), notificationId);
    }

    @PostMapping("/read-all")
    @Operation(
            summary = "Read all notifications",
            description = "Mark all notifications as read",
            operationId = "3_readAllNotifications"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully read all"))
    public void readAllNotifications(@Authentication AuthInfo authInfo) {
        notificationService.readAllNotifications(authInfo.getUserId());
    }

}
