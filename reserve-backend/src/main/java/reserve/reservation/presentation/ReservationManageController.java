package reserve.reservation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.notification.service.NotificationService;
import reserve.reservation.service.ReservationManageService;

@RestController
@RequestMapping("/v1/reservations/manage")
@RequiredArgsConstructor
@Tag(name = "Reservation Managements", description = "Reservation API")
public class ReservationManageController {

    private final ReservationManageService reservationManageService;

    private final NotificationService notificationService;

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancel reservation by reservation ID",
            operationId = "1_cancel")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully canceled"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public void cancel(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        reservationManageService.cancel(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Reservation has been cancelled.",
                "Customer has cancelled the reservation.");
    }

    @PostMapping("/{reservationId}/start")
    @Operation(summary = "Start service", description = "Change reservation status to 'In Service' by reservation ID",
            operationId = "2_startService")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully started"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public void startService(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        reservationManageService.startService(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Service has been started.");
    }

    @PostMapping("/{reservationId}/complete")
    @Operation(summary = "Complete service", description = "Change reservation status to 'Completed' by reservation ID",
            operationId = "3_complete")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully completed"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public void complete(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        reservationManageService.complete(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Service has been completed.");
    }

}
