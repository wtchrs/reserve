package reserve.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.store.infrastructure.StoreQueryRepository;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreQueryRepository storeQueryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long userId, StoreCreateRequest storeCreateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO);
        }
        Store store = storeRepository.save(new Store(
                userRepository.getReferenceById(userId),
                storeCreateRequest.getName(),
                storeCreateRequest.getPrice(),
                storeCreateRequest.getAddress(),
                storeCreateRequest.getDescription()
        ));
        return store.getId();
    }

    @Transactional(readOnly = true)
    public StoreInfoResponse getStoreInfo(Long storeId) {
        return storeRepository.findResponseById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public StoreInfoListResponse search(StoreSearchRequest storeSearchRequest, Pageable pageable) {
        Page<StoreInfoResponse> page = storeQueryRepository.findResponsesBySearch(storeSearchRequest, pageable);
        return StoreInfoListResponse.from(page);
    }

    @Transactional
    public void update(Long userId, Long storeId, StoreUpdateRequest storeUpdateRequest) {
        Store store = storeRepository.findByIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND));
        if (StringUtils.hasText(storeUpdateRequest.getName())) {
            store.setName(storeUpdateRequest.getName());
        }
        if (storeUpdateRequest.getPrice() != null) {
            store.setPrice(storeUpdateRequest.getPrice());
        }
        if (StringUtils.hasText(storeUpdateRequest.getAddress())) {
            store.setAddress(storeUpdateRequest.getAddress());
        }
        if (StringUtils.hasText(storeUpdateRequest.getDescription())) {
            store.setAddress(storeUpdateRequest.getDescription());
        }
    }

    @Transactional
    public void delete(Long userId, Long storeId) {
        if (!storeRepository.existsByIdAndUserId(storeId, userId)) {
            throw new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND);
        }
        storeRepository.deleteById(storeId);
    }

}
