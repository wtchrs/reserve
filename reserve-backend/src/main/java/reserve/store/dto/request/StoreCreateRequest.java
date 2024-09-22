package reserve.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StoreCreateRequest {

    @Schema(description = "Name of the store", example = "store name")
    @NotEmpty(message = "Name required.")
    private String name;

    @Schema(description = "Address of the store", example = "store address")
    @NotEmpty(message = "Address required.")
    private String address;

    @Schema(description = "Description of the store", example = "store description")
    @NotEmpty(message = "Description required.")
    private String description;

}
