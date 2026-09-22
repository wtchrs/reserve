package reserve.reservation.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.dto.response.ReservationMenuListResponse;
import reserve.reservation.dto.response.ReservationMenuResponse;
import reserve.reservation.infrastructure.ReservationMenuRepository;
import reserve.reservation.infrastructure.ReservationQueryRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.reservation.infrastructure.ReservationSlotRepository;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.infrastructure.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationQueryRepository reservationQueryRepository;

    @Mock
    ReservationMenuRepository reservationMenuRepository;

    @Mock
    ReservationSlotRepository reservationSlotRepository;

    @Mock
    MenuRepository menuRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReservationService reservationService;

    ReservationMenuCreateRequest createReservationMenuCreateRequest(Long menuId, int quantity) {
        ReservationMenuCreateRequest request = new ReservationMenuCreateRequest();
        request.setMenuId(menuId);
        request.setQuantity(quantity);
        return request;
    }

    Menu createMenuMock(Store store, ReservationMenuCreateRequest request) {
        Menu menu = Mockito.mock(Menu.class);
        Mockito.when(menu.getId()).thenReturn(request.getMenuId());
        Mockito.when(menu.getStore()).thenReturn(store);
        return menu;
    }

    @Test
    @DisplayName("Testing reservation creation")
    void testReservationCreation() {
        ReservationMenuCreateRequest menuCreateRequest1 = createReservationMenuCreateRequest(10L, 1);
        ReservationMenuCreateRequest menuCreateRequest2 = createReservationMenuCreateRequest(20L, 2);

        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(1L);
        reservationCreateRequest.setDate(LocalDate.now());
        reservationCreateRequest.setHour(1);
        reservationCreateRequest.setMenus(List.of(menuCreateRequest1, menuCreateRequest2));

        Store storeMock = Mockito.mock();
        Mockito.when(storeMock.getId()).thenReturn(1L);

        Menu menuMock1 = createMenuMock(storeMock, menuCreateRequest1);
        Menu menuMock2 = createMenuMock(storeMock, menuCreateRequest2);

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

        Mockito.when(reservationRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(storeRepository.getReferenceById(1L)).thenReturn(storeMock);

        Mockito.when(menuRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(menuMock1, menuMock2));
        Mockito
            .when(reservationSlotRepository.tryAcquire(reservationCreateRequest.getStoreId(),
                    reservationCreateRequest.getDate(), reservationCreateRequest.getHour()))
            .thenReturn(true);

        try (MockedConstruction<Reservation> ignored = Mockito.mockConstruction(Reservation.class, (mock, context) -> {
            Mockito.when(mock.getId()).thenReturn(1L);
            Mockito.when(mock.getStore()).thenReturn((Store) context.arguments().get(1));
        })) {
            Long result = reservationService.create(1L, reservationCreateRequest);
            assertEquals(1L, result);
        }

        Mockito.verify(reservationMenuRepository, Mockito.times(1)).saveAll(Mockito.anyList());
        Mockito.verify(reservationSlotRepository, Mockito.times(1))
            .createIfAbsent(reservationCreateRequest.getStoreId(), reservationCreateRequest.getDate(),
                    reservationCreateRequest.getHour());
    }

    @Test
    @DisplayName("Testing retrieval of reservation information")
    void testReservationInfoRetrieval() {
        ReservationInfoResponse reservationInfoResponse = new ReservationInfoResponse(1L, 1L, "registrant", "username",
                LocalDate.now(), 1);
        Mockito.when(reservationRepository.findResponseByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(reservationInfoResponse));

        ReservationInfoResponse response = reservationService.getReservationInfo(1L, 1L);

        assertEquals(reservationInfoResponse, response);
    }

    @Test
    void getReservationMenus() {
        ReservationMenuResponse response1 = Mockito.mock(ReservationMenuResponse.class);
        ReservationMenuResponse response2 = Mockito.mock(ReservationMenuResponse.class);
        ReservationMenuResponse response3 = Mockito.mock(ReservationMenuResponse.class);

        Mockito.when(reservationQueryRepository.hasReadAccessToReservation(1L, 1L)).thenReturn(true);
        Mockito.when(reservationMenuRepository.findResponsesByReservationId(1L))
            .thenReturn(List.of(response1, response2, response3));
        ReservationMenuListResponse reservationMenus = reservationService.getReservationMenus(1L, 1L);

        assertEquals(3, reservationMenus.getCount());
        assertThat(reservationMenus.getResults()).contains(response1, response2, response3);
    }

    @Test
    @DisplayName("Testing reservation search functionality")
    void testReservationSearch() {
        ReservationSearchRequest reservationSearchRequest = new ReservationSearchRequest();
        PageRequest pageable = PageRequest.of(0, 20);
        ReservationInfoResponse reservationInfo1 = new ReservationInfoResponse(1L, 1L, "registrant", "username",
                LocalDate.now(), 1);
        ReservationInfoResponse reservationInfo2 = new ReservationInfoResponse(1L, 1L, "registrant", "username",
                LocalDate.now(), 2);
        ReservationInfoResponse reservationInfo3 = new ReservationInfoResponse(1L, 1L, "registrant", "username",
                LocalDate.now(), 3);

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(reservationQueryRepository.findResponsesBySearch(1L, reservationSearchRequest, pageable))
            .thenReturn(new PageImpl<>(List.of(reservationInfo1, reservationInfo2, reservationInfo3), pageable, 3));

        ReservationInfoListResponse response = reservationService.search(1L, reservationSearchRequest, pageable);

        assertEquals(3, response.getCount());
        assertEquals(3, response.getResults().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertFalse(response.hasNext());
        assertThat(response.getResults()).contains(reservationInfo1, reservationInfo2, reservationInfo3);
    }

    @Test
    @DisplayName("Testing reservation update functionality")
    void testReservationUpdating() {
        LocalDate oldDate = LocalDate.now();
        LocalDate newDate = oldDate.plusDays(1);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setDate(newDate);
        request.setHour(10);

        Reservation reservation = new Reservation(Mockito.mock(), Mockito.mock(), oldDate, 1);
        Mockito.when(reservation.getStore().getId()).thenReturn(1L);

        Mockito.when(reservationRepository.findByIdAndUserIdForUpdate(1L, 1L)).thenReturn(Optional.of(reservation));
        Mockito.when(reservationSlotRepository.tryAcquire(1L, request.getDate(), request.getHour())).thenReturn(true);

        reservationService.update(1L, 1L, request);

        assertEquals(request.getDate(), reservation.getDate());
        assertEquals(request.getHour(), reservation.getHour());

        Mockito.verify(reservationSlotRepository, Mockito.times(1))
            .createIfAbsent(1L, request.getDate(), request.getHour());
        Mockito.verify(reservationSlotRepository, Mockito.times(1)).release(1L, oldDate, 1);
    }

    @Test
    @DisplayName("Testing reservation deletion functionality")
    void testReservationDeletion() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        Reservation reservation = Mockito.mock();
        Mockito.when(reservation.getStore()).thenReturn(Mockito.mock());
        Mockito.when(reservation.getStore().getId()).thenReturn(1L);
        Mockito.when(reservation.getDate()).thenReturn(date);
        Mockito.when(reservation.getHour()).thenReturn(13);
        Mockito.when(reservation.cancel()).thenReturn(true);

        Mockito.when(reservationRepository.findByIdAndUserIdForUpdate(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationService.cancel(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).cancel();
        Mockito.verify(reservationSlotRepository, Mockito.times(1)).release(1L, date, 13);
    }

}
