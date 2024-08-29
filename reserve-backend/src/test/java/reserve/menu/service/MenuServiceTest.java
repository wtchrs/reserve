package reserve.menu.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reserve.menu.domain.Menu;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.menu.infrastructure.MenuQueryRepository;
import reserve.menu.infrastructure.MenuRepository;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    StoreRepository storeRepository;

    @Mock
    MenuRepository menuRepository;

    @Mock
    MenuQueryRepository menuQueryRepository;

    @InjectMocks
    MenuService menuService;

    @Test
    @DisplayName("Testing menu creation")
    void testMenuCreation() {
        Menu menuMock = Mockito.mock(Menu.class);
        Mockito.when(menuMock.getId()).thenReturn(1L);
        Mockito.when(menuRepository.save(Mockito.any(Menu.class))).thenReturn(menuMock);

        Store storeMock = Mockito.mock(Store.class);
        Mockito.when(storeMock.getId()).thenReturn(1L);
        Mockito.when(storeRepository.getReferenceById(1L)).thenReturn(storeMock);

        Mockito.when(storeRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        MenuCreateRequest menuCreateRequest = new MenuCreateRequest();
        menuCreateRequest.setName("Spaghetti Aglio e Olio");
        menuCreateRequest.setPrice(1000);
        menuCreateRequest.setDescription("Spaghetti with garlic and olive oil");

        Long menuId = menuService.create(1L, 1L, menuCreateRequest);

        assertEquals(1L, menuId);

        Mockito.verify(menuRepository).save(Mockito.argThat(menu -> {
            assertEquals(1L, menu.getStore().getId());
            assertEquals("Spaghetti Aglio e Olio", menu.getName());
            assertEquals(1000, menu.getPrice());
            assertEquals("Spaghetti with garlic and olive oil", menu.getDescription());
            return true;
        }));
    }

    @Test
    @DisplayName("Testing menu information retrieval")
    void testMenuInfoRetrieval() {
        MenuInfoResponse response = new MenuInfoResponse(
                1L,
                1L,
                "Spaghetti Aglio e Olio",
                1000,
                "Spaghetti with garlic and olive oil"
        );
        Mockito.when(menuRepository.findResponseById(1L)).thenReturn(Optional.of(response));

        MenuInfoResponse result = menuService.getMenuInfo(1L);

        assertEquals(response, result);
    }

    @Test
    @DisplayName("Testing all menus of store retrieval")
    void testStoreMenusRetrieval() {
        MenuInfoResponse response1 = new MenuInfoResponse(
                1L,
                1L,
                "Spaghetti Aglio e Olio",
                1000,
                "Spaghetti with garlic and olive oil"
        );
        MenuInfoResponse response2 = new MenuInfoResponse(
                2L,
                1L,
                "Spaghetti Carbonara",
                1200,
                "Spaghetti with bacon, eggs, and cheese"
        );
        MenuInfoResponse response3 = new MenuInfoResponse(
                3L,
                1L,
                "Spaghetti Bolognese",
                1200,
                "Spaghetti with meat sauce"
        );

        Mockito.when(menuRepository.findResponsesByStoreId(1L)).thenReturn(List.of(response1, response2, response3));

        MenuInfoListResponse result = menuService.getStoreMenus(1L);

        assertEquals(3, result.getCount());
        Assertions.assertThat(result.getResults())
                .extracting(MenuInfoResponse::getMenuId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Testing menu update")
    void testMenuUpdate() {
        Menu menu = Mockito.mock(Menu.class);
        Mockito.when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        Mockito.when(menuQueryRepository.hasAccessToMenu(1L, 1L)).thenReturn(true);

        MenuUpdateRequest menuUpdateRequest = new MenuUpdateRequest();
        menuUpdateRequest.setName("Spaghetti Aglio e Olio");
        menuUpdateRequest.setPrice(1000);
        menuUpdateRequest.setDescription("Spaghetti with garlic and olive oil");

        menuService.update(1L, 1L, menuUpdateRequest);

        Mockito.verify(menu).setName("Spaghetti Aglio e Olio");
        Mockito.verify(menu).setPrice(1000);
        Mockito.verify(menu).setDescription("Spaghetti with garlic and olive oil");
    }

    @Test
    @DisplayName("Testing menu deletion")
    void testMenuDeletion() {
        Mockito.when(menuQueryRepository.hasAccessToMenu(1L, 1L)).thenReturn(true);

        menuService.delete(1L, 1L);

        Mockito.verify(menuRepository).deleteById(1L);
    }

}
