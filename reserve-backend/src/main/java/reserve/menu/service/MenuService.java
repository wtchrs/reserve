package reserve.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.InvalidAccessException;
import reserve.global.exception.ResourceNotFoundException;
import reserve.menu.domain.Menu;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.menu.infrastructure.MenuQueryRepository;
import reserve.menu.infrastructure.MenuRepository;
import reserve.store.infrastructure.StoreRepository;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final StoreRepository storeRepository;

    private final MenuRepository menuRepository;

    private final MenuQueryRepository menuQueryRepository;

    @Transactional
    public Long create(Long userId, Long storeId, MenuCreateRequest menuCreateRequest) {
        if (!storeRepository.existsByIdAndUserId(storeId, userId)) {
            throw new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND);
        }
        Menu menu = menuRepository.save(new Menu(
                storeRepository.getReferenceById(storeId),
                menuCreateRequest.getName(),
                menuCreateRequest.getPrice(),
                menuCreateRequest.getDescription()
        ));
        return menu.getId();
    }

    @Transactional(readOnly = true)
    public MenuInfoResponse getMenuInfo(Long menuId) {
        return menuRepository.findResponseById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MENU_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public MenuInfoListResponse getStoreMenus(Long storeId) {
        return MenuInfoListResponse.from(menuRepository.findResponsesByStoreId(storeId));
    }

    @Transactional
    public void update(Long userId, Long menuId, MenuUpdateRequest menuUpdateRequest) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MENU_NOT_FOUND));
        if (!menuQueryRepository.hasAccessToMenu(menuId, userId)) {
            throw new InvalidAccessException(ErrorCode.ACCESS_DENIED);
        }
        if (menuUpdateRequest.getName() != null) {
            menu.setName(menuUpdateRequest.getName());
        }
        if (menuUpdateRequest.getPrice() != null) {
            menu.setPrice(menuUpdateRequest.getPrice());
        }
        if (menuUpdateRequest.getDescription() != null) {
            menu.setDescription(menuUpdateRequest.getDescription());
        }
    }

    @Transactional
    public void delete(Long userId, Long menuId) {
        if (!menuQueryRepository.hasAccessToMenu(menuId, userId)) {
            throw new InvalidAccessException(ErrorCode.ACCESS_DENIED);
        }
        menuRepository.deleteById(menuId);
    }

}
