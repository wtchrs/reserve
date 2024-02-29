package reserve.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;

@Service
@RequiredArgsConstructor
public class ReservationManageService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public void cancel(Long registrantId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, registrantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.cancel();
    }

    @Transactional
    public void startService(Long registrantId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, registrantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.start();
    }

    @Transactional
    public void complete(Long registrantId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, registrantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.complete();
    }

}
