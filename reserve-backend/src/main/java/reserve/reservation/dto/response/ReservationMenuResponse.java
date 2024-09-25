package reserve.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;

public interface ReservationMenuResponse {

    @Schema(description = "ID of reservation menu", example = "1")
    @Value("#{target.id}")
    Long getReservationMenuId();

    @Schema(description = "Name of menu", example = "Pizza")
    String getName();

    @Schema(description = "Price of menu", example = "10000")
    int getPrice();

    @Schema(description = "Quantity", example = "1")
    int getQuantity();

}
