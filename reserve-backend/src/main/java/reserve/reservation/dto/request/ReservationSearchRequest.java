package reserve.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.global.validation.NullOrNotEmpty;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ReservationSearchRequest {

    @Schema(description = "Type of search (REGISTRANT, CUSTOMER)",
            example = "REGISTRANT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Type required.")
    private SearchType type;

    @Schema(description = "Query string for name, address, and description of the store",
            example = "store query string")
    @NullOrNotEmpty(message = "Query must not be empty string.")
    private String query;

    @Schema(description = "Date of the reservation", example = "2025-01-01")
    private LocalDate date;

    public enum SearchType {
        REGISTRANT, CUSTOMER
    }

}
