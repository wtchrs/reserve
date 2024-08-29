package reserve.reservation.dto.response;

import org.springframework.beans.factory.annotation.Value;

public interface ReservationMenuResponse {

    @Value("#{target.id}")
    Long getReservationMenuId();

    String getName();

    int getPrice();

    int getQuantity();

}
