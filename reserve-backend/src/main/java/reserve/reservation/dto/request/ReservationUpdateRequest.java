package reserve.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ReservationUpdateRequest {

    @Schema(description = "Date of the reservation",
            example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Date required.")
    private LocalDate date;

    @Schema(description = "Hour of the reservation (24-hour format)",
            example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hour required.")
    @Min(value = 0, message = "Hour must be between 0 and 23.")
    @Max(value = 23, message = "Hour must be between 0 and 23.")
    private int hour;

}
