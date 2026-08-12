package reserve.reservation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.notification.service.NotificationService;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.dto.response.ReservationMenuListResponse;
import reserve.reservation.service.ReservationService;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation API")
public class ReservationController {

    private final ReservationService reservationService;

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create reservation", description = "Create reservation", operationId = "1_create")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully reserved"))
    @ApiErrorCodeResponses({ @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO),
            @ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND) })
    public ResponseEntity<Void> create(@Authentication AuthInfo authInfo,
            @RequestBody @Validated ReservationCreateRequest reservationCreateRequest) {
        Long reservationId = reservationService.create(authInfo.getUserId(), reservationCreateRequest);
        notificationService.notifyReservation(reservationId, "Reservation has been created.",
                "New customer has made a reservation.");
        return ResponseEntity.created(URI.create("/v1/reservations/" + reservationId)).build();
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get reservation info", description = "Get reservation info by reservation ID",
            operationId = "2_getReservationInfo")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with reservation info",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservationInfoResponse.class))))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public ReservationInfoResponse getReservationInfo(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        return reservationService.getReservationInfo(authInfo.getUserId(), reservationId);
    }

    @GetMapping("/{reservationId}/menus")
    @Operation(summary = "Get reservation menus", description = "Get reservation menus by reservation ID",
            operationId = "3_getReservationMenus")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with reservation menus",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservationMenuListResponse.class))))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED))
    public ReservationMenuListResponse getReservationMenus(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        return reservationService.getReservationMenus(authInfo.getUserId(), reservationId);
    }

    @GetMapping
    @Operation(summary = "Search reservations", description = "Search reservations by store ID, date, and hour",
            operationId = "4_search")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with reservation info list",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservationInfoListResponse.class))))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO))
    public ReservationInfoListResponse search(@Authentication AuthInfo authInfo,
            @ModelAttribute @Validated @ParameterObject ReservationSearchRequest reservationSearchRequest,
            @ParameterObject Pageable pageable) {
        return reservationService.search(authInfo.getUserId(), reservationSearchRequest, pageable);
    }

    @PutMapping("/{reservationId}")
    @Operation(summary = "Update reservation", description = "Update reservation by reservation ID",
            operationId = "5_update")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public void update(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation", example = "1") Long reservationId,
            @RequestBody @Validated ReservationUpdateRequest reservationUpdateRequest) {
        reservationService.update(authInfo.getUserId(), reservationId, reservationUpdateRequest);
        notificationService.notifyReservation(reservationId, "Reservation has been updated.",
                "Customer has updated the reservation.");
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancel reservation by reservation ID",
            operationId = "6_cancel")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully canceled"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    public void cancel(@Authentication AuthInfo authInfo,
            @PathVariable("reservationId") @Schema(description = "ID of reservation",
                    example = "1") Long reservationId) {
        reservationService.cancel(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Reservation has been canceled.",
                "Customer has canceled the reservation.");
    }

}
