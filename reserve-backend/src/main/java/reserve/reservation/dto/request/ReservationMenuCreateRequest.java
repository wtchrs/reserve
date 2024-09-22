package reserve.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReservationMenuCreateRequest {

    @Schema(description = "Menu ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Menu ID required.")
    private Long menuId;

    @Schema(description = "Quantity", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private int quantity;

}
