package reserve.store.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reserve.store.domain.Store;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StoreRepositoryTest {

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Testing store response retrieval by ID")
    void testStoreRetrieval() {
        User user = userRepository.save(new User("username", "password", "hello", "description"));
        Store store = storeRepository.save(new Store(user, "name", 1000, "address", "description"));

        storeRepository.findResponseById(store.getId()).ifPresentOrElse(storeInfoResponse -> {
            assertEquals(store.getId(), storeInfoResponse.getStoreId());
            assertEquals(store.getUser().getUsername(), storeInfoResponse.getRegistrant());
            assertEquals(store.getName(), storeInfoResponse.getName());
            assertEquals(store.getPrice(), storeInfoResponse.getPrice());
            assertEquals(store.getAddress(), storeInfoResponse.getAddress());
            assertEquals(store.getDescription(), storeInfoResponse.getDescription());
        }, () -> fail("Store not found"));
    }

    @Test
    @DisplayName("Testing store deletion by ID")
    void testStoreDeletion() {
        User user = userRepository.save(new User("username", "password", "hello", "description"));
        Store store = storeRepository.save(new Store(user, "name", 1000, "address", "description"));

        storeRepository.deleteById(store.getId());

        assertFalse(storeRepository.existsById(store.getId()));
    }

}