package reserve.store.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreSearchRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.store.infrastructure.StoreQueryRepository;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    StoreRepository storeRepository;

    @Mock
    StoreQueryRepository storeQueryRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    StoreService storeService;

    @Test
    @DisplayName("Testing store creation")
    void testStoreCreation() throws Exception {
        StoreCreateRequest storeCreateRequest = Mockito.spy(new StoreCreateRequest());
        Mockito.when(storeCreateRequest.getName()).thenReturn("name");
        Mockito.when(storeCreateRequest.getAddress()).thenReturn("address");
        Mockito.when(storeCreateRequest.getDescription()).thenReturn("description");
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(storeRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedConstruction<Store> ignored = Mockito.mockConstruction(
                Store.class,
                (mock, context) -> Mockito.when(mock.getId()).thenReturn(1L)
        )) {
            // Created store id
            Long result = storeService.create(1L, storeCreateRequest);

            assertEquals(1L, result);
            Mockito.verify(storeRepository).save(Mockito.any());
        }
    }

    @Test
    @DisplayName("Testing retrieval of store information")
    void testStoreInfoRetrieval() {
        StoreInfoResponse response = new StoreInfoResponse(1L, "username", "name", "address", "description");
        Mockito.when(storeRepository.findResponseById(1L)).thenReturn(Optional.of(response));

        StoreInfoResponse result = storeService.getStoreInfo(1L);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("Testing store search")
    void testStoreSearch() {
        StoreSearchRequest storeSearchRequest = Mockito.mock(StoreSearchRequest.class);
        Pageable pageable = PageRequest.of(0, 20);

        StoreInfoResponse storeInfo1 = new StoreInfoResponse(1L, "username", "Pasta", "address", "Pasta");
        StoreInfoResponse storeInfo2 =
                new StoreInfoResponse(1L, "username", "Italian", "address", "Steak and pasta");
        StoreInfoResponse storeInfo3 =
                new StoreInfoResponse(1L, "username", "Pizza", "address", "Pizza and pasta");
        Mockito.when(storeQueryRepository.findResponsesBySearch(storeSearchRequest, pageable))
                .thenReturn(new PageImpl<>(List.of(storeInfo1, storeInfo2, storeInfo3), pageable, 3));

        StoreInfoListResponse response = storeService.search(storeSearchRequest, pageable);

        assertEquals(3, response.getCount());
        assertEquals(3, response.getResults().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertFalse(response.hasNext());
        assertThat(response.getResults()).contains(storeInfo1, storeInfo2, storeInfo3);
    }

    @Test
    @DisplayName("Testing store update")
    void testStoreUpdate() {
        Store store = Mockito.spy(new Store(Mockito.mock(User.class), "name", "address", "description"));
        Mockito.when(storeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(store));

        StoreUpdateRequest storeUpdateRequest = Mockito.spy(new StoreUpdateRequest());
        Mockito.when(storeUpdateRequest.getName()).thenReturn("newName");
        Mockito.when(storeUpdateRequest.getAddress()).thenReturn("newAddress");
        Mockito.when(storeUpdateRequest.getDescription()).thenReturn("newDescription");

        storeService.update(1L, 1L, storeUpdateRequest);

        Mockito.verify(store).setName("newName");
        Mockito.verify(store).setAddress("newAddress");
        Mockito.verify(store).setDescription("newDescription");
    }

    @Test
    @DisplayName("Testing store deletion")
    void testStoreDeletion() {
        Mockito.when(storeRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        storeService.delete(1L, 1L);

        Mockito.verify(storeRepository).deleteById(1L);
    }

}