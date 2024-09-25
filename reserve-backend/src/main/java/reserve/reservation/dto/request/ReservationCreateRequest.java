package reserve.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ReservationCreateRequest {

    @Schema(description = "Store ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Store information is wrong.")
    private Long storeId;

    @Schema(description = "Date", example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Date required.")
    private LocalDate date;

    @Schema(description = "Hour", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hour required.")
    @Min(value = 0, message = "Hour must be between 0 and 23.")
    @Max(value = 23, message = "Hour must be between 0 and 23.")
    private int hour;

    @Schema(description = "Menus", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "Menus are available up to 100 items.")
    private List<ReservationMenuCreateRequest> menus = List.of();

}
