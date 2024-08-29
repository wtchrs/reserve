package reserve.reservation.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReservationMenuListResponse {

    private final long count;

    private final List<ReservationMenuResponse> results;

    public static ReservationMenuListResponse from(List<ReservationMenuResponse> responses) {
        return new ReservationMenuListResponse(
                responses.size(),
                responses
        );
    }

}
