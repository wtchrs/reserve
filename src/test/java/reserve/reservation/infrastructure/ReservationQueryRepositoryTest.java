package reserve.reservation.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import reserve.reservation.domain.Reservation;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationQueryRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationQueryRepository reservationQueryRepository;

    @Test
    @DisplayName("Verifying reservation existence by ID and user ID")
    @Transactional
    void testReservationExistence() {
        User user = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store = storeRepository.save(new Store(user, "Pasta", 1000, "address", "Pasta only"));
        Reservation reservation = reservationRepository.save(new Reservation(user, store, LocalDate.now(), 12));

        assertTrue(reservationQueryRepository.existsByIdAndUserId(reservation.getId(), user.getId()));

        reservationRepository.deleteById(reservation.getId());

        assertFalse(reservationQueryRepository.existsByIdAndUserId(reservation.getId(), user.getId()));
    }

    @Test
    @DisplayName("Testing reservation search by user ID, search criteria, and date")
    void testReservationSearch() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        User user2 = userRepository.save(new User("user2", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));
        Store store2 = storeRepository.save(new Store(user1, "Pizza", 1000, "address", "Pizza and Pasta"));
        Store store3 = storeRepository.save(new Store(user1, "Hamburger", 1000, "pasta street", "Hamburger"));
        Store store4 = storeRepository.save(new Store(user1, "Korean food", 1000, "address", "Kimchi and Bulgogi"));
        Store store5 = storeRepository.save(new Store(user2, "Italian", 1000, "address", "Steak and Pasta"));
        Store store6 = storeRepository.save(new Store(user2, "Ramen", 1000, "address", "Ramen and Gyoza"));

        reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store2, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store2, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store3, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store4, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store5, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user1, store6, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store1, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store2, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store3, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store4, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store5, LocalDate.now(), 12));
        reservationRepository.save(new Reservation(user2, store6, LocalDate.now(), 12));

        ReservationSearchRequest request = Mockito.mock(ReservationSearchRequest.class);
        Mockito.when(request.getType()).thenReturn(ReservationSearchRequest.SearchType.CUSTOMER);
        Mockito.when(request.getQuery()).thenReturn("pasta");
        Mockito.when(request.getDate()).thenReturn(LocalDate.now());

        Page<ReservationInfoResponse> response =
                reservationQueryRepository.findResponsesBySearch(user1.getId(), request, PageRequest.of(0, 20));

        assertEquals(5, response.getTotalElements());
        response.forEach(reservationInfoResponse -> {
            assertEquals(user1.getUsername(), reservationInfoResponse.getReservationName());
            assertThat(reservationInfoResponse.getStoreId())
                    .isIn(store1.getId(), store2.getId(), store3.getId(), store5.getId());
        });

        // clean up
        reservationRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }

}