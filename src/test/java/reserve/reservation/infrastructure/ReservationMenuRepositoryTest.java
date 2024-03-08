package reserve.reservation.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationMenu;
import reserve.reservation.dto.response.ReservationMenuResponse;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationMenuRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuRepository reservationMenuRepository;

    Reservation reservation;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("username", "password", "nickname", "description"));
        User registrant = userRepository.save(new User("registrant", "password", "nickname", "description"));
        Store store = storeRepository.save(new Store(registrant, "storeName", "address", "description"));
        reservation = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName1", 10000, 2));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName2", 5000, 1));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName3", 15000, 1));
    }

    @Test
    @DisplayName("Testing reservation menu retrieval by reservation id")
    void testReservationMenuRetrievalByReservationId() {
        List<ReservationMenuResponse> responses =
                reservationMenuRepository.findResponsesByReservationId(reservation.getId());

        assertEquals(3, responses.size());
        Assertions.assertThat(responses).extracting("name").containsExactly("menuName1", "menuName2", "menuName3");
        Assertions.assertThat(responses).extracting("price").containsExactly(10000, 5000, 15000);
        Assertions.assertThat(responses).extracting("quantity").containsExactly(2, 1, 1);
    }

}
