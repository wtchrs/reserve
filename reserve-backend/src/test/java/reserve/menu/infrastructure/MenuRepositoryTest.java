package reserve.menu.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.menu.domain.Menu;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MenuRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    User user;
    Store store;
    Menu menu1, menu2, menu3;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("user1", "password", "hello", "description"));
        store = storeRepository.save(new Store(user, "Pasta", "address", "Pasta only"));
        menu1 = menuRepository.save(new Menu(
                store,
                "Spaghetti Aglio e Olio",
                1000,
                "Spaghetti with garlic and olive oil"
        ));
        menu2 = menuRepository.save(new Menu(
                store,
                "Spaghetti Carbonara",
                1200,
                "Spaghetti with bacon, eggs, and cheese"
        ));
        menu3 = menuRepository.save(new Menu(
                store,
                "Spaghetti Bolognese",
                1200,
                "Spaghetti with meat sauce"
        ));
    }

    @Test
    @DisplayName("Testing menu response retrieval by ID")
    void testMenuResponseRetrievalById() {
        menuRepository.findResponseById(menu1.getId()).ifPresentOrElse(
                menuInfoResponse -> {
                    assertEquals(menu1.getId(), menuInfoResponse.getMenuId());
                    assertEquals(store.getId(), menuInfoResponse.getStoreId());
                    assertEquals(menu1.getName(), menuInfoResponse.getName());
                    assertEquals(menu1.getPrice(), menuInfoResponse.getPrice());
                    assertEquals(menu1.getDescription(), menuInfoResponse.getDescription());
                },
                () -> fail("Menu not found")
        );
    }

    @Test
    @DisplayName("Testing menu responses list retrieval by store ID")
    void testMenuResponsesRetrievalByStoreId() {
        List<MenuInfoResponse> result = menuRepository.findResponsesByStoreId(store.getId());

        assertEquals(3, result.size());
        Assertions.assertThat(result)
                .extracting(MenuInfoResponse::getMenuId)
                .containsExactly(menu1.getId(), menu2.getId(), menu3.getId());

        result.forEach(menuInfoResponse -> {
            if (menuInfoResponse.getMenuId().equals(menu1.getId())) {
                assertEquals(store.getId(), menuInfoResponse.getStoreId());
                assertEquals(menu1.getName(), menuInfoResponse.getName());
                assertEquals(menu1.getPrice(), menuInfoResponse.getPrice());
                assertEquals(menu1.getDescription(), menuInfoResponse.getDescription());
            } else if (menuInfoResponse.getMenuId().equals(menu2.getId())) {
                assertEquals(store.getId(), menuInfoResponse.getStoreId());
                assertEquals(menu2.getName(), menuInfoResponse.getName());
                assertEquals(menu2.getPrice(), menuInfoResponse.getPrice());
                assertEquals(menu2.getDescription(), menuInfoResponse.getDescription());
            } else if (menuInfoResponse.getMenuId().equals(menu3.getId())) {
                assertEquals(store.getId(), menuInfoResponse.getStoreId());
                assertEquals(menu3.getName(), menuInfoResponse.getName());
                assertEquals(menu3.getPrice(), menuInfoResponse.getPrice());
                assertEquals(menu3.getDescription(), menuInfoResponse.getDescription());
            } else {
                fail("Not expected result");
            }
        });
    }

    @Test
    @DisplayName("Testing menu deletion by ID")
    void testMenuDeletionById() {
        menuRepository.deleteById(menu1.getId());
        assertFalse(menuRepository.existsById(menu1.getId()));
    }

}
