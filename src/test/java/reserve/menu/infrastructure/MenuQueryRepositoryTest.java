package reserve.menu.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.menu.domain.Menu;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MenuQueryRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    MenuQueryRepository menuQueryRepository;

    User user1, user2;
    Store store;
    Menu menu;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        user2 = userRepository.save(new User("user2", "password", "world", "description"));
        store = storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));
        menu = menuRepository.save(new Menu(store, "Spaghetti Aglio e Olio", 1000, "Spaghetti with garlic and olive oil"));
    }

    @Test
    @DisplayName("Testing menu access check")
    void testCheckingAccessToMenu() {
        assertTrue(menuQueryRepository.hasAccessToMenu(menu.getId(), user1.getId()));
        assertFalse(menuQueryRepository.hasAccessToMenu(menu.getId(), user2.getId()));
    }

}
