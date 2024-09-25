package reserve.menu.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reserve.auth.domain.AuthInfo;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;

@Tag(name = "Menus", description = "Menu API")
public interface MenuOperations {

    @Operation(
            summary = "Create menu",
            description = "Create a menu for a store",
            operationId = "1_createMenu"
    )
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Menu created"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND))
    @SuppressWarnings("unused")
    ResponseEntity<Void> createMenu(
            AuthInfo authInfo,
            @Schema(description = "Store ID", example = "1") Long storeId,
            MenuCreateRequest menuCreateRequest
    );


    @Operation(
            summary = "Get menu information",
            description = "Get menu information by menu ID",
            operationId = "2_getMenuInfo"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with menu information",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuInfoResponse.class)
            )
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.MENU_NOT_FOUND))
    @SuppressWarnings("unused")
    MenuInfoResponse getMenuInfo(@Schema(description = "Menu ID", example = "1") Long menuId);


    @Operation(
            summary = "Get store menus",
            description = "Get menus of a store by store ID",
            operationId = "3_getStoreMenus"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with store menus",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuInfoListResponse.class)
            )
    ))
    @SuppressWarnings("unused")
    MenuInfoListResponse getStoreMenus(@Schema(description = "Store ID", example = "1") Long storeId);


    @Operation(
            summary = "Update menu",
            description = "Update menu information by menu ID",
            operationId = "4_updateMenu"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Menu updated"))
    @ApiErrorCodeResponses({
            @ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.MENU_NOT_FOUND),
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED)
    })
    @SuppressWarnings("unused")
    void updateMenu(
            AuthInfo authInfo,
            @Schema(description = "Menu ID", example = "1") Long menuId,
            MenuUpdateRequest menuUpdateRequest
    );


    @Operation(
            summary = "Delete menu",
            description = "Delete menu by menu ID",
            operationId = "5_deleteMenu"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Menu deleted"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.ACCESS_DENIED))
    @SuppressWarnings("unused")
    void deleteMenu(AuthInfo authInfo, @Schema(description = "Menu ID", example = "1") Long menuId);

}
