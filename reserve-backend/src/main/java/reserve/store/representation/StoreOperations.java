package reserve.store.representation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import reserve.auth.domain.AuthInfo;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;

@Tag(name = "Stores", description = "Store API")
public interface StoreOperations {

    @Operation(
            summary = "Create store",
            description = "Create a store",
            operationId = "1_createStore"
    )
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created"))
    @SuppressWarnings("unused")
    ResponseEntity<Void> create(AuthInfo authInfo, StoreCreateRequest storeCreateRequest);


    @Operation(
            summary = "Get store information",
            description = "Get store information by store ID",
            operationId = "2_getStoreInfo"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with store information",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StoreInfoResponse.class)
            )
    ))
    @SuppressWarnings("unused")
    StoreInfoResponse getStoreInfo(
            @Schema(description = "Store ID", example = "1") Long storeId
    );


    @Operation(
            summary = "Search stores",
            description = "Search stores by username of registrant and query string",
            operationId = "3_searchStores"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with store information list",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StoreInfoListResponse.class)
            )
    ))
    @SuppressWarnings("unused")
    StoreInfoListResponse search(
            @ParameterObject StoreSearchRequest storeSearchRequest,
            @ParameterObject Pageable pageable
    );


    @Operation(
            summary = "Update store information",
            description = "Update store information by store ID",
            operationId = "4_updateStore"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @SuppressWarnings("unused")
    void update(
            AuthInfo authInfo,
            @Schema(description = "Store ID", example = "1") Long storeId,
            StoreUpdateRequest storeUpdateRequest
    );


    @Operation(
            summary = "Delete store",
            description = "Delete store by store ID",
            operationId = "5_deleteStore"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully deleted"))
    @SuppressWarnings("unused")
    void delete(
            AuthInfo authInfo,
            @Schema(description = "Store ID", example = "1") Long storeId
    );

}
