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

    @Min(value = 0, message = "Price must be larger than 0.")
    private int price;

    @NotEmpty(message = "Address required.")
    private String address;

    @NotEmpty(message = "Description required.")
    private String description;

}
