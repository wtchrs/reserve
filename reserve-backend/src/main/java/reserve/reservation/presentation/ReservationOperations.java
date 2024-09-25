package reserve.reservation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import reserve.auth.domain.AuthInfo;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.dto.response.ReservationMenuListResponse;

@Tag(name = "Reservations", description = "Reservation API")
public interface ReservationOperations {

    @Operation(
            summary = "Create reservation",
            description = "Create reservation",
            operationId = "1_create"
    )
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully reserved"))
    @ApiErrorCodeResponses({
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO),
            @ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND)
    })
    @SuppressWarnings("unused")
    ResponseEntity<Void> create(AuthInfo authInfo, ReservationCreateRequest reservationCreateRequest);


    @Operation(
            summary = "Get reservation info",
            description = "Get reservation info by reservation ID",
            operationId = "2_getReservationInfo"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with reservation info",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationInfoResponse.class)
            )
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    @SuppressWarnings("unused")
    ReservationInfoResponse getReservationInfo(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );


    @Operation(
            summary = "Get reservation menus",
            description = "Get reservation menus by reservation ID",
            operationId = "3_getReservationMenus"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with reservation menus",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationMenuListResponse.class)
            )
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED))
    @SuppressWarnings("unused")
    ReservationMenuListResponse getReservationMenus(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );


    @Operation(
            summary = "Search reservations",
            description = "Search reservations by store ID, date, and hour",
            operationId = "4_search"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with reservation info list",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationInfoListResponse.class)
            )
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO))
    @SuppressWarnings("unused")
    ReservationInfoListResponse search(
            AuthInfo authInfo,
            @ParameterObject ReservationSearchRequest reservationSearchRequest,
            @ParameterObject Pageable pageable
    );


    @Operation(
            summary = "Update reservation",
            description = "Update reservation by reservation ID",
            operationId = "5_update"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    @SuppressWarnings("unused")
    void update(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId,
            ReservationUpdateRequest reservationUpdateRequest
    );


    @Operation(
            summary = "Cancel reservation",
            description = "Cancel reservation by reservation ID",
            operationId = "6_cancel"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully canceled"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.RESERVATION_NOT_FOUND))
    @SuppressWarnings("unused")
    void cancel(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );

}
