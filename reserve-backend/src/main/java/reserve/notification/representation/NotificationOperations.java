package reserve.notification.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import reserve.auth.domain.AuthInfo;
import reserve.notification.dto.response.NotificationInfoListResponse;

@Tag(name = "Notifications", description = "Notification API")
public interface NotificationOperations {

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
    @SuppressWarnings("unused")
    NotificationInfoListResponse getUserNotifications(AuthInfo authInfo, @ParameterObject Pageable pageable);


    @Operation(
            summary = "Read notification",
            description = "Mark notification as read by notification ID",
            operationId = "2_readNotification"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully read"))
    @SuppressWarnings("unused")
    void readNotification(
            AuthInfo authInfo,
            @Schema(description = "Notification ID", example = "1") Long notificationId
    );


    @Operation(
            summary = "Read all notifications",
            description = "Mark all notifications as read",
            operationId = "3_readAllNotifications"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully read all"))
    @SuppressWarnings("unused")
    void readAllNotifications(AuthInfo authInfo);

}
