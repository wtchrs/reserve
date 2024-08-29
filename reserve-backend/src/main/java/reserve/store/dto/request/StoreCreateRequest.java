package reserve.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StoreCreateRequest {

    @NotEmpty(message = "Name required.")
    private String name;

    @NotEmpty(message = "Address required.")
    private String address;

    @NotEmpty(message = "Description required.")
    private String description;

}
