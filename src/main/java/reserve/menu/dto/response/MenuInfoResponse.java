package reserve.menu.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MenuInfoResponse {

    private final Long menuId;
    private final Long storeId;
    private final String name;
    private final int price;
    private final String description;

}
