package reserve.reservation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.notification.service.NotificationService;
import reserve.reservation.service.ReservationManageService;

@RestController
@RequestMapping("/v1/reservations/manage")
@RequiredArgsConstructor
public class ReservationManageController implements ReservationManageOperations {

    private final ReservationManageService reservationManageService;
    private final NotificationService notificationService;

    @Override
    @PostMapping("/{reservationId}/cancel")
    public void cancel(@Authentication AuthInfo authInfo, @PathVariable("reservationId") Long reservationId) {
        reservationManageService.cancel(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(
                reservationId,
                "Reservation has been cancelled.",
                "Customer has cancelled the reservation."
        );
    }

    @Override
    @PostMapping("/{reservationId}/start")
    public void startService(@Authentication AuthInfo authInfo, @PathVariable("reservationId") Long reservationId) {
        reservationManageService.startService(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Service has been started.");
    }

    @Override
    @PostMapping("/{reservationId}/complete")
    public void complete(@Authentication AuthInfo authInfo, @PathVariable("reservationId") Long reservationId) {
        reservationManageService.complete(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(reservationId, "Service has been completed.");
    }

}
