package reserve.store.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.store.service.StoreService;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store API")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "Create store", description = "Create a store", operationId = "1_createStore")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO))
    public ResponseEntity<Void> create(@Authentication AuthInfo authInfo,
            @RequestBody @Validated StoreCreateRequest storeCreateRequest) {
        Long storeId = storeService.create(authInfo.getUserId(), storeCreateRequest);
        return ResponseEntity.created(URI.create("/v1/stores/" + storeId)).build();
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "Get store information", description = "Get store information by store ID",
            operationId = "2_getStoreInfo")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with store information",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StoreInfoResponse.class))))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND))
    public StoreInfoResponse getStoreInfo(
            @PathVariable("storeId") @Schema(description = "Store ID", example = "1") Long storeId) {
        return storeService.getStoreInfo(storeId);
    }

    @GetMapping
    @Operation(summary = "Search stores", description = "Search stores by username of registrant and query string",
            operationId = "3_searchStores")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Response with store information list",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StoreInfoListResponse.class))))
    public StoreInfoListResponse search(
            @ModelAttribute @Validated @ParameterObject StoreSearchRequest storeSearchRequest,
            @ParameterObject Pageable pageable) {
        return storeService.search(storeSearchRequest, pageable);
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Update store information", description = "Update store information by store ID",
            operationId = "4_updateStore")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND))
    public void update(@Authentication AuthInfo authInfo,
            @PathVariable("storeId") @Schema(description = "Store ID", example = "1") Long storeId,
            @RequestBody @Validated StoreUpdateRequest storeUpdateRequest) {
        storeService.update(authInfo.getUserId(), storeId, storeUpdateRequest);
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Delete store", description = "Delete store by store ID", operationId = "5_deleteStore")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully deleted"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.STORE_NOT_FOUND))
    public void delete(@Authentication AuthInfo authInfo,
            @PathVariable("storeId") @Schema(description = "Store ID", example = "1") Long storeId) {
        storeService.delete(authInfo.getUserId(), storeId);
    }

}
