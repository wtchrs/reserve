package reserve.store.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StoreUpdateRequest {

    private String name;

    @Min(value = 0, message = "Price must be larger than 0.")
    private Integer price;

    private String address;
    private String description;

}
