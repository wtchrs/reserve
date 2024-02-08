package reserve.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.reservation.infrastructure.validator.NullOrNotEmpty;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ReservationSearchRequest {

    @NotNull(message = "Type required.")
    private SearchType type;

    @NullOrNotEmpty(message = "Query must not be empty string.")
    private String query;

    private LocalDate date;

    public enum SearchType {
        REGISTRANT, CUSTOMER
    }

}
