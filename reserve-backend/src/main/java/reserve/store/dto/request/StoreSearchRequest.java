package reserve.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class StoreSearchRequest {

    @Schema(description = "Username of registrant", example = "username")
    @Size(min = 0, message = "'registrant' cannot be empty string.")
    private String registrant;

    @Schema(description = "Query string for store name, description, and address", example = "query")
    @Size(min = 0, message = "'query' cannot be empty string.")
    private String query;

}
