package reserve.reservation.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class ReservationInfoResponse {

    private final Long reservationId;

    private final Long storeId;

    private final String registrant;

    private final String reservationName;

    private final LocalDate date;

    private final int hour;

}
