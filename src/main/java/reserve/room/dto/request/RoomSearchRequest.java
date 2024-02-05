package reserve.room.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RoomSearchRequest {

    @Size(min = 0, message = "'registrant' cannot be empty string.")
    private String registrant;

    @Size(min = 0, message = "'query' cannot be empty string.")
    private String query;

}
