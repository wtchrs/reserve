package reserve.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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

    @Schema(description = "New capacity of the store's each slot. `-1` means no capacity limitation.",
            examples = { "-1", "0", "5" }, defaultValue = "-1")
    @Min(value = -1, message = "Capacity must equal to or larger than -1.")
    private Integer capacity;

}
