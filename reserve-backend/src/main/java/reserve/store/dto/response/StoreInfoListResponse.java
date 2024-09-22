package reserve.store.dto.response;

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
public class StoreInfoListResponse {

    @Schema(description = "Total number of stores", example = "1")
    private final long count;

    @Schema(description = "Number of stores per page", example = "1")
    private final int pageSize;

    @Schema(description = "Current page number", example = "1")
    private final int pageNumber;

    @Getter(AccessLevel.PRIVATE)
    private final boolean hasNext;

    @Schema(description = "List of store information")
    private final List<StoreInfoResponse> results;

    @Schema(description = "Whether there is a next page", example = "false")
    @JsonProperty("hasNext")
    public boolean hasNext() {
        return this.hasNext;
    }

    public static StoreInfoListResponse from(Page<StoreInfoResponse> page) {
        return new StoreInfoListResponse(
                page.getTotalElements(),
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber(),
                page.hasNext(),
                page.getContent()
        );
    }

}
