package reserve.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@RequiredArgsConstructor
@Getter
@JsonPropertyOrder({"count", "pageSize", "pageNumber", "hasNext", "results"})
public class NotificationInfoListResponse {

    @Schema(description = "Number of results", example = "1")
    private final long count;

    @Schema(description = "Page size", example = "20")
    private final int pageSize;

    @Schema(description = "Page number", example = "1")
    private final int pageNumber;

    @Getter(AccessLevel.PRIVATE)
    private final boolean hasNext;

    @Schema(description = "List of notification info")
    private final List<NotificationInfo> results;

    @Schema(description = "Whether there is a next page", example = "false")
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
