package reserve.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StoreInfoResponse {

    @Schema(description = "Store ID", example = "1")
    private final Long storeId;

    @Schema(description = "Username of registrant", example = "username")
    private final String registrant;

    @Schema(description = "Name of the store", example = "store name")
    private final String name;

    @Schema(description = "Address of the store", example = "store address")
    private final String address;

    @Schema(description = "Description of the store", example = "store description")
    private final String description;

}
