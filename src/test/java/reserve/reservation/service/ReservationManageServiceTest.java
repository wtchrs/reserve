package reserve.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservationManageServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    ReservationManageService reservationManageService;

    @Test
    @DisplayName("Testing cancellation of reservation")
    void testReservationCancellation() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationManageService.cancel(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).cancel();
    }

    @Test
    @DisplayName("Testing start of reservation service")
    void restReservationStartService() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationManageService.startService(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).start();
    }

    @Test
    @DisplayName("Testing completion of reservation")
    void testReservationCompletion() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationManageService.complete(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).complete();
    }

}
