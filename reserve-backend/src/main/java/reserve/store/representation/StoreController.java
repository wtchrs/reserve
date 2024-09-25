package reserve.store.representation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.store.service.StoreService;

import java.net.URI;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
public class StoreController implements StoreOperations {

    private final StoreService storeService;

    @Override
    @PostMapping
    public ResponseEntity<Void> create(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated StoreCreateRequest storeCreateRequest
    ) {
        Long storeId = storeService.create(authInfo.getUserId(), storeCreateRequest);
        return ResponseEntity.created(URI.create("/v1/stores/" + storeId)).build();
    }

    @Override
    @GetMapping("/{storeId}")
    public StoreInfoResponse getStoreInfo(@PathVariable("storeId") Long storeId) {
        return storeService.getStoreInfo(storeId);
    }

    @Override
    @GetMapping
    public StoreInfoListResponse search(
            @ModelAttribute @Validated StoreSearchRequest storeSearchRequest,
            Pageable pageable
    ) {
        return storeService.search(storeSearchRequest, pageable);
    }

    @Override
    @PutMapping("/{storeId}")
    public void update(
            @Authentication AuthInfo authInfo,
            @PathVariable("storeId") Long storeId,
            @RequestBody @Validated StoreUpdateRequest storeUpdateRequest
    ) {
        storeService.update(authInfo.getUserId(), storeId, storeUpdateRequest);
    }

    @Override
    @DeleteMapping("/{storeId}")
    public void delete(@Authentication AuthInfo authInfo, @PathVariable("storeId") Long storeId) {
        storeService.delete(authInfo.getUserId(), storeId);
    }

}
