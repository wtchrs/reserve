package reserve.notification.dto.response;

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
public class NotificationInfoListResponse {

    private final long count;
    private final int pageSize;
    private final int pageNumber;

    @Getter(AccessLevel.PRIVATE)
    private final boolean hasNext;

    private final List<NotificationInfo> results;

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return this.hasNext;
    }

    public static NotificationInfoListResponse from(Page<NotificationInfo> page) {
        return new NotificationInfoListResponse(
                page.getTotalElements(),
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber(),
                page.hasNext(),
                page.getContent()
        );
    }

}
