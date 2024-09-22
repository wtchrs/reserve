package reserve.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.global.validation.NullOrNotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class StoreUpdateRequest {

    @Schema(description = "New name of the store", example = "store name")
    @NullOrNotEmpty
    private String name;

    @Schema(description = "New address of the store", example = "store address")
    @NullOrNotEmpty
    private String address;

    @Schema(description = "New description of the store", example = "store description")
    @NullOrNotEmpty
    private String description;

}
