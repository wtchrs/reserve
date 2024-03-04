package reserve.menu.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.global.validation.NullOrNotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class MenuUpdateRequest {

    @NullOrNotEmpty
    private String name;

    @Min(0)
    private Integer price;

    @NullOrNotEmpty
    private String description;

}
