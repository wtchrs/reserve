package reserve.reservation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReservationForNotifyDto {

    private final Long reservationId;

    private final Long userId;

    private final Long registrantId;

}
