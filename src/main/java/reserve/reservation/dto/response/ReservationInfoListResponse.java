package reserve.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@RequiredArgsConstructor
@Getter
@JsonPropertyOrder({"count", "pageSize", "pageNumber", "hasNext", "results"})
public class ReservationInfoListResponse {

    private final long count;
    private final int pageSize;
    private final int pageNumber;

    @Getter(AccessLevel.PRIVATE)
    private final boolean hasNext;

    private final List<ReservationInfoResponse> results;

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return this.hasNext;
    }

    public static ReservationInfoListResponse from(Page<ReservationInfoResponse> page) {
        return new ReservationInfoListResponse(
                page.getTotalElements(),
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber(),
                page.hasNext(),
                page.getContent()
        );
    }

}
