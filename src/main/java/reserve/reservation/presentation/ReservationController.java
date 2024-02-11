package reserve.reservation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.notification.service.NotificationService;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.service.ReservationService;

import java.net.URI;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> create(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated ReservationCreateRequest reservationCreateRequest
    ) {
        Long reservationId = reservationService.create(authInfo.getUserId(), reservationCreateRequest);
        notificationService.notifyReservation(
                authInfo.getUserId(),
                reservationId,
                "Reservation has been created.",
                "New customer has made a reservation."
        );
        return ResponseEntity.created(URI.create("/v1/reservations/" + reservationId)).build();
    }

    @GetMapping("/{reservationId}")
    public ReservationInfoResponse getReservationInfo(
            @Authentication AuthInfo authInfo,
            @PathVariable("reservationId") Long reservationId
    ) {
        return reservationService.getReservationInfo(authInfo.getUserId(), reservationId);
    }

    @GetMapping
    public ReservationInfoListResponse search(
            @Authentication AuthInfo authInfo,
            @ModelAttribute @Validated ReservationSearchRequest reservationSearchRequest,
            Pageable pageable
    ) {
        return reservationService.search(authInfo.getUserId(), reservationSearchRequest, pageable);
    }

    @PutMapping("/{reservationId}")
    public void update(
            @Authentication AuthInfo authInfo,
            @PathVariable("reservationId") Long reservationId,
            @RequestBody @Validated ReservationUpdateRequest reservationUpdateRequest
    ) {
        reservationService.update(authInfo.getUserId(), reservationId, reservationUpdateRequest);
        notificationService.notifyReservation(
                authInfo.getUserId(),
                reservationId,
                "Reservation has been updated.",
                "Customer has updated the reservation."
        );
    }

    @DeleteMapping("/{reservationId}")
    public void delete(@Authentication AuthInfo authInfo, @PathVariable("reservationId") Long reservationId) {
        reservationService.delete(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(
                authInfo.getUserId(),
                reservationId,
                "Reservation has been canceled.",
                "Customer has canceled the reservation."
        );
    }

}
