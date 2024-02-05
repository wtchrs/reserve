package reserve.room.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RoomInfoResponse {

    private final Long roomId;
    private final String registrant; // username
    private final String name;
    private final int price;
    private final String address;
    private final String description;

}
