package reserve.store.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reserve.global.validation.NullOrNotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class StoreUpdateRequest {

    @NullOrNotEmpty
    private String name;

    @NullOrNotEmpty
    private String address;

    @NullOrNotEmpty
    private String description;

}
