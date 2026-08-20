package reserve.store.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import reserve.support.IntegrationTest;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.support.TestStateCleaner;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

@IntegrationTest
@Import(TestStateCleaner.class)
class StoreQueryRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    StoreQueryRepository storeQueryRepository;

    @Autowired
    TestStateCleaner testStateCleaner;

    /**
     * This data setup method commits the changes to update the full-text index.
     */
    @Transactional
    @Commit
    @BeforeEach
    void setUp() {
        User user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        User user2 = userRepository.save(new User("user2", "password", "hello", "description"));
        storeRepository.save(new Store(user1, "Pasta", "address", "Pasta only"));
        storeRepository.save(new Store(user1, "Pizza", "address", "Pizza and Pasta"));
        storeRepository.save(new Store(user1, "Hamburger", "pasta street", "Hamburger"));
        storeRepository.save(new Store(user1, "Korean food", "address", "Kimchi and Bulgogi"));
        storeRepository.save(new Store(user2, "Italian", "address", "Steak and Pasta"));
        storeRepository.save(new Store(user2, "Ramen", "address", "Ramen and Gyoza"));
    }

    @Transactional
    @Commit
    @AfterEach
    void cleanUp() {
        testStateCleaner.cleanUp();
    }

    @Test
    @DisplayName("Testing store search by user and query")
    void testStoreSearchByUserAndQuery() {
        StoreSearchRequest request = Mockito.mock(StoreSearchRequest.class);
        Mockito.when(request.getRegistrant()).thenReturn("user1");
        Mockito.when(request.getQuery()).thenReturn("pasta");
        Pageable pageable = PageRequest.of(0, 20);

        // Committed data is required to use the full-text index.
        Page<StoreInfoResponse> response = storeQueryRepository.findResponsesBySearch(request, pageable);

        assertEquals(3, response.getTotalElements());
        response.forEach(storeInfoResponse -> {
            assertEquals("user1", storeInfoResponse.getRegistrant());
            assertTrue(storeInfoResponse.getName().toLowerCase().contains("pasta")
                    || storeInfoResponse.getDescription().toLowerCase().contains("pasta")
                    || storeInfoResponse.getAddress().toLowerCase().contains("pasta"));
        });
    }

}
