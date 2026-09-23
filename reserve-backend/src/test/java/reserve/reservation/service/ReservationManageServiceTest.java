package reserve.reservation.service;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.reservation.infrastructure.ReservationSlotRepository;
import reserve.store.domain.Store;

@ExtendWith(MockitoExtension.class)
class ReservationManageServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationSlotRepository reservationSlotRepository;

    @InjectMocks
    ReservationManageService reservationManageService;

    @Test
    @DisplayName("Testing cancellation of reservation")
    void testReservationCancellation() {
        Store store = Mockito.mock(Store.class);
        Mockito.when(store.getId()).thenReturn(1L);

        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getStore()).thenReturn(store);
        Mockito.when(reservation.getDate()).thenReturn(LocalDate.of(2026, 1, 1));
        Mockito.when(reservation.getHour()).thenReturn(13);
        Mockito.when(reservation.cancel()).thenReturn(true);

        Mockito.when(reservationRepository.findByIdAndStoreUserIdForUpdate(1L, 1L))
            .thenReturn(Optional.of(reservation));

        reservationManageService.cancel(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).cancel();
        Mockito.verify(reservationSlotRepository, Mockito.times(1))
            .release(store.getId(), reservation.getDate(), reservation.getHour());
    }

    @Test
    @DisplayName("Testing start of reservation service")
    void restReservationStartService() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndStoreUserIdForUpdate(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationManageService.startService(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).start();
    }

    @Test
    @DisplayName("Testing completion of reservation")
    void testReservationCompletion() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndStoreUserIdForUpdate(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationManageService.complete(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).complete();
    }

}
