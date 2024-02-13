package reserve.reservation.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.reservation.domain.Reservation;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("Testing store user ID retrieval by reservation ID, including deleted reservations")
    void testStoreUserIdRetrieval() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));

        Reservation deleted = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));
        reservationRepository.deleteById(deleted.getId());
        Reservation reservation = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 14));

        reservationRepository.findStoreUserIdByIdIncludeDeleted(deleted.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
        reservationRepository.findStoreUserIdByIdIncludeDeleted(reservation.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
    }

    @Test
    @DisplayName("Testing response retrieval by reservation ID and user ID, excluding deleted reservations.")
    void testResponseRetrieval() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));

        Reservation deleted = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));
        reservationRepository.deleteById(deleted.getId());
        Reservation reservation = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 14));

        reservationRepository.findResponseByIdAndUserId(deleted.getId(), user1.getId())
                .ifPresent(unused -> fail("Reservation found"));
        reservationRepository.findResponseByIdAndUserId(reservation.getId(), user1.getId()).ifPresentOrElse(
                response -> assertEquals(reservation.getId(), response.getReservationId()),
                () -> fail("Reservation not found")
        );
    }

    @Test
    @DisplayName("Testing reservation deletion by ID and its retrievability.")
    void testReservationDeletion() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));
        Reservation reservation = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));

        reservationRepository.deleteById(reservation.getId());

        // not to use cache
        em.refresh(reservation);

        reservationRepository.findById(reservation.getId()).ifPresentOrElse(
                findReservation -> assertTrue(findReservation.isDeleted()),
                () -> fail("Reservation not found")
        );
    }

}