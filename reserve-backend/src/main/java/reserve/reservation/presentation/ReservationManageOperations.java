package reserve.reservation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reserve.auth.domain.AuthInfo;

@Tag(name = "Reservation Managements", description = "Reservation API")
public interface ReservationManageOperations {

    @Operation(
            summary = "Cancel reservation",
            description = "Cancel reservation by reservation ID",
            operationId = "1_cancel"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully canceled"))
    @SuppressWarnings("unused")
    void cancel(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );


    @Operation(
            summary = "Start service",
            description = "Change reservation status to 'In Service' by reservation ID",
            operationId = "2_startService"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully started"))
    @SuppressWarnings("unused")
    void startService(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );


    @Operation(
            summary = "Complete service",
            description = "Change reservation status to 'Completed' by reservation ID",
            operationId = "3_complete"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully completed"))
    @SuppressWarnings("unused")
    void complete(
            AuthInfo authInfo,
            @Schema(description = "ID of reservation", example = "1") Long reservationId
    );

}
