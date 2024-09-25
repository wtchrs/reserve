package reserve.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReservationMenuListResponse {

    @Schema(description = "Number of results", example = "1")
    private final long count;

    @Schema(description = "List of reservation menus")
    private final List<ReservationMenuResponse> results;

    public static ReservationMenuListResponse from(List<ReservationMenuResponse> responses) {
        return new ReservationMenuListResponse(
                responses.size(),
                responses
        );
    }

}
