package reserve.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReservationMenuCreateRequest {

    @NotNull(message = "Menu ID required.")
    private Long menuId;

    @NotNull(message = "Quantity required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private int quantity;

}
