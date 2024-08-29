package reserve.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MenuCreateRequest {

    @NotEmpty
    private String name;

    @NotNull
    @Min(0)
    private int price;

    @NotEmpty
    private String description;

}
