package reserve.reservation.dto.request;

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
public class ReservationCreateRequest {

    @NotNull(message = "Store information is wrong.")
    private Long storeId;

    @NotNull(message = "Date required.")
    private LocalDate date;

    @NotNull(message = "Hour required.")
    @Min(value = 0, message = "Hour must be between 0 and 23.")
    @Max(value = 23, message = "Hour must be between 0 and 23.")
    private int hour;

}
