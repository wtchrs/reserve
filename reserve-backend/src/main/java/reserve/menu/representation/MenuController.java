package reserve.menu.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.menu.service.MenuService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Menus", description = "Menu API")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/v1/stores/{storeId}/menus")
    @Operation(summary = "Create menu", description = "Create a menu for a store", operationId = "1_createMenu")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Menu created"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND))
    public ResponseEntity<Void> createMenu(@Authentication AuthInfo authInfo,
            @PathVariable("storeId") @Schema(description = "Store ID", example = "1") Long storeId,
            @RequestBody @Validated MenuCreateRequest menuCreateRequest) {
        Long id = menuService.create(authInfo.getUserId(), storeId, menuCreateRequest);
        return ResponseEntity.created(URI.create("/v1/menus/" + id)).build();
    }

    @GetMapping("/v1/menus/{menuId}")
    @Operation(summary = "Get menu information", description = "Get menu information by menu ID",
            operationId = "2_getMenuInfo")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with menu information",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MenuInfoResponse.class))))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.MENU_NOT_FOUND))
    public MenuInfoResponse getMenuInfo(
            @PathVariable("menuId") @Schema(description = "Menu ID", example = "1") Long menuId) {
        return menuService.getMenuInfo(menuId);
    }

    @GetMapping("/v1/stores/{storeId}/menus")
    @Operation(summary = "Get store menus", description = "Get menus of a store by store ID",
            operationId = "3_getStoreMenus")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with store menus",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MenuInfoListResponse.class))))
    public MenuInfoListResponse getStoreMenus(
            @PathVariable("storeId") @Schema(description = "Store ID", example = "1") Long storeId) {
        return menuService.getStoreMenus(storeId);
    }

    @PutMapping("/v1/menus/{menuId}")
    @Operation(summary = "Update menu", description = "Update menu information by menu ID",
            operationId = "4_updateMenu")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Menu updated"))
    @ApiErrorCodeResponses({ @ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.MENU_NOT_FOUND),
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED) })
    public void updateMenu(@Authentication AuthInfo authInfo,
            @PathVariable("menuId") @Schema(description = "Menu ID", example = "1") Long menuId,
            @RequestBody @Validated MenuUpdateRequest menuUpdateRequest) {
        menuService.update(authInfo.getUserId(), menuId, menuUpdateRequest);
    }

    @DeleteMapping("/v1/menus/{menuId}")
    @Operation(summary = "Delete menu", description = "Delete menu by menu ID", operationId = "5_deleteMenu")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Menu deleted"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED))
    public void deleteMenu(@Authentication AuthInfo authInfo,
            @PathVariable("menuId") @Schema(description = "Menu ID", example = "1") Long menuId) {
        menuService.delete(authInfo.getUserId(), menuId);
    }

}
