package reserve.menu.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MenuInfoListResponse {

    private final long count;

    private final List<MenuInfoResponse> results;

    public static MenuInfoListResponse from(List<MenuInfoResponse> content) {
        return new MenuInfoListResponse(content.size(), content);
    }

}
