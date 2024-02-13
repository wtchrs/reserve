package reserve.store.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StoreQueryRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    StoreQueryRepository storeQueryRepository;

    @Transactional
    @Commit
    @BeforeEach
    void setUp() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        User user2 = userRepository.save(new User("user2", "password", "hello", "description"));
        storeRepository.save(new Store(user1, "Pasta", 1000, "address", "Pasta only"));
        storeRepository.save(new Store(user1, "Pizza", 1000, "address", "Pizza and Pasta"));
        storeRepository.save(new Store(user1, "Hamburger", 1000, "pasta street", "Hamburger"));
        storeRepository.save(new Store(user1, "Korean food", 1000, "address", "Kimchi and Bulgogi"));
        storeRepository.save(new Store(user2, "Italian", 1000, "address", "Steak and Pasta"));
        storeRepository.save(new Store(user2, "Ramen", 1000, "address", "Ramen and Gyoza"));
    }

    @Transactional
    @Commit
    @AfterEach
    void tearDown() {
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Testing store search by user and query")
    void testStoreSearchByUserAndQuery() {
        StoreSearchRequest request = Mockito.mock(StoreSearchRequest.class);
        Mockito.when(request.getRegistrant()).thenReturn("user1");
        Mockito.when(request.getQuery()).thenReturn("pasta");
        Pageable pageable = PageRequest.of(0, 20);

        Page<StoreInfoResponse> response = storeQueryRepository.findResponsesBySearch(request, pageable);

        assertEquals(3, response.getTotalElements());
        response.forEach(storeInfoResponse -> {
            assertEquals("user1", storeInfoResponse.getRegistrant());
            assertTrue(
                    storeInfoResponse.getName().toLowerCase().contains("pasta") ||
                    storeInfoResponse.getDescription().toLowerCase().contains("pasta") ||
                    storeInfoResponse.getAddress().toLowerCase().contains("pasta")
            );
        });
    }

}