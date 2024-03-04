package reserve.reservation.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationStatusType;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("Testing store user ID retrieval by reservation ID, including reservations in all states")
    void testStoreUserIdRetrieval() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", "address", "Pasta only"));

        Reservation reservationReady = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));
        Reservation reservationInService =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 14));
        reservationInService.start();
        Reservation reservationCompleted =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 16));
        reservationCompleted.start();
        reservationCompleted.complete();
        Reservation reservationCancelled =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 18));
        reservationCancelled.cancel();

        reservationRepository.findStoreUserIdById(reservationReady.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
        reservationRepository.findStoreUserIdById(reservationInService.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
        reservationRepository.findStoreUserIdById(reservationCompleted.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
        reservationRepository.findStoreUserIdById(reservationCancelled.getId()).ifPresentOrElse(
                storeRegistrantId -> assertEquals(user1.getId(), storeRegistrantId),
                () -> fail("Store not found")
        );
    }

    @Test
    @DisplayName("Testing response retrieval by reservation ID and user ID, for reservations in all states.")
    void testResponseRetrieval() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", "address", "Pasta only"));

        Reservation reservationReady = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));
        Reservation reservationInService =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 14));
        reservationInService.start();
        Reservation reservationCompleted =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 16));
        reservationCompleted.start();
        reservationCompleted.complete();
        Reservation reservationCancelled =
                reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 18));
        reservationCancelled.cancel();

        reservationRepository.findResponseByIdAndUserId(reservationReady.getId(), user1.getId()).ifPresentOrElse(
                response -> assertEquals(reservationReady.getId(), response.getReservationId()),
                () -> fail("Reservation not found")
        );
        reservationRepository.findResponseByIdAndUserId(reservationInService.getId(), user1.getId()).ifPresentOrElse(
                response -> assertEquals(reservationInService.getId(), response.getReservationId()),
                () -> fail("Reservation not found")
        );
        reservationRepository.findResponseByIdAndUserId(reservationCompleted.getId(), user1.getId()).ifPresentOrElse(
                response -> assertEquals(reservationCompleted.getId(), response.getReservationId()),
                () -> fail("Reservation not found")
        );
        reservationRepository.findResponseByIdAndUserId(reservationCancelled.getId(), user1.getId()).ifPresentOrElse(
                response -> assertEquals(reservationCancelled.getId(), response.getReservationId()),
                () -> fail("Reservation not found")
        );
    }

    @Test
    @DisplayName("Testing reservation state changes.")
    void testReservationStateChanges() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        Store store1 = storeRepository.save(new Store(user1, "Pasta", "address", "Pasta only"));
        Reservation reservation = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 12));

        reservation.start();
        assertEquals(ReservationStatusType.IN_SERVICE, reservation.getStatus());

        reservation.complete();
        assertEquals(ReservationStatusType.COMPLETED, reservation.getStatus());

        reservation = reservationRepository.save(new Reservation(user1, store1, LocalDate.now(), 14));
        reservation.cancel();
        assertEquals(ReservationStatusType.CANCELLED, reservation.getStatus());
    }

}