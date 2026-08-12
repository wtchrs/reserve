package reserve.reservation.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import reserve.reservation.domain.ReservationMenu;
import reserve.reservation.dto.response.ReservationMenuResponse;

public interface ReservationMenuRepository extends JpaRepository<ReservationMenu, Long> {

    List<ReservationMenuResponse> findResponsesByReservationId(Long reservationId);

}
