package reserve.menu.representation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.menu.service.MenuService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/v1/stores/{storeId}/menus")
    public ResponseEntity<Void> createMenu(
            @Authentication AuthInfo authInfo,
            @PathVariable("storeId") Long storeId,
            @RequestBody @Validated MenuCreateRequest menuCreateRequest
    ) {
        Long id = menuService.create(authInfo.getUserId(), storeId, menuCreateRequest);
        return ResponseEntity.created(URI.create("/v1/menus/" + id)).build();
    }

    @GetMapping("/v1/menus/{menuId}")
    public MenuInfoResponse getMenuInfo(@PathVariable("menuId") Long menuId) {
        return menuService.getMenuInfo(menuId);
    }

    @GetMapping("/v1/stores/{storeId}/menus")
    public MenuInfoListResponse getStoreMenus(@PathVariable("storeId") Long storeId) {
        return menuService.getStoreMenus(storeId);
    }

    @PutMapping("/v1/menus/{menuId}")
    public void updateMenu(
            @Authentication AuthInfo authInfo,
            @PathVariable("menuId") Long menuId,
            @RequestBody @Validated MenuUpdateRequest menuUpdateRequest
    ) {
        menuService.update(authInfo.getUserId(), menuId, menuUpdateRequest);
    }

    @DeleteMapping("/v1/menus/{menuId}")
    public void deleteMenu(@Authentication AuthInfo authInfo, @PathVariable("menuId") Long menuId) {
        menuService.delete(authInfo.getUserId(), menuId);
    }

}
