package reserve.reservation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class ReservationUpdateRequest {

    @NotNull(message = "Date required.")
    private LocalDate date;

    @NotNull(message = "Hour required.")
    @Min(value = 0, message = "Hour must be between 0 and 23.")
    @Max(value = 23, message = "Hour must be between 0 and 23.")
    private int hour;

}
