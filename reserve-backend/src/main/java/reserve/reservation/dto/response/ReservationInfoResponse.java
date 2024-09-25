package reserve.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class ReservationInfoResponse {

    @Schema(description = "ID of reservation", example = "1")
    private final Long reservationId;

    @Schema(description = "ID of store", example = "1")
    private final Long storeId;

    @Schema(description = "username of the store's registrant", example = "storeowner123")
    private final String registrant;

    @Schema(description = "Username of the person making the reservation", example = "user123")
    private final String reservationName;

    @Schema(description = "Date of the reservation", example = "2025-01-01")
    private final LocalDate date;

    @Schema(description = "Hour of the reservation (24-hour format)", example = "12")
    private final int hour;

}
