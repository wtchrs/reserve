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
import reserve.reservation.dto.response.ReservationMenuListResponse;
import reserve.reservation.service.ReservationService;

import java.net.URI;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationOperations {

    private final ReservationService reservationService;

    private final NotificationService notificationService;

    @Override
    @PostMapping
    public ResponseEntity<Void> create(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated ReservationCreateRequest reservationCreateRequest
    ) {
        Long reservationId = reservationService.create(authInfo.getUserId(), reservationCreateRequest);
        notificationService.notifyReservation(
                reservationId,
                "Reservation has been created.",
                "New customer has made a reservation."
        );
        return ResponseEntity.created(URI.create("/v1/reservations/" + reservationId)).build();
    }

    @Override
    @GetMapping("/{reservationId}")
    public ReservationInfoResponse getReservationInfo(
            @Authentication AuthInfo authInfo,
            @PathVariable("reservationId") Long reservationId
    ) {
        return reservationService.getReservationInfo(authInfo.getUserId(), reservationId);
    }

    @Override
    @GetMapping("/{reservationId}/menus")
    public ReservationMenuListResponse getReservationMenus(
            @Authentication AuthInfo authInfo,
            @PathVariable("reservationId") Long reservationId
    ) {
        return reservationService.getReservationMenus(authInfo.getUserId(), reservationId);
    }

    @Override
    @GetMapping
    public ReservationInfoListResponse search(
            @Authentication AuthInfo authInfo,
            @ModelAttribute @Validated ReservationSearchRequest reservationSearchRequest,
            Pageable pageable
    ) {
        return reservationService.search(authInfo.getUserId(), reservationSearchRequest, pageable);
    }

    @Override
    @PutMapping("/{reservationId}")
    public void update(
            @Authentication AuthInfo authInfo,
            @PathVariable("reservationId") Long reservationId,
            @RequestBody @Validated ReservationUpdateRequest reservationUpdateRequest
    ) {
        reservationService.update(authInfo.getUserId(), reservationId, reservationUpdateRequest);
        notificationService.notifyReservation(
                reservationId,
                "Reservation has been updated.",
                "Customer has updated the reservation."
        );
    }

    @Override
    @PostMapping("/{reservationId}/cancel")
    public void cancel(@Authentication AuthInfo authInfo, @PathVariable("reservationId") Long reservationId) {
        reservationService.cancel(authInfo.getUserId(), reservationId);
        notificationService.notifyReservation(
                reservationId,
                "Reservation has been canceled.",
                "Customer has canceled the reservation."
        );
    }

}
