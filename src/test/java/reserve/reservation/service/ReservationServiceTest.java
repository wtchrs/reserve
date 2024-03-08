package reserve.reservation.service;

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
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationQueryRepository reservationQueryRepository;

    @Mock
    ReservationMenuRepository reservationMenuRepository;

    @Mock
    MenuRepository menuRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReservationService reservationService;

    @Test
    @DisplayName("Testing reservation creation")
    void testReservationCreation() {
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(1L);
        reservationCreateRequest.setDate(LocalDate.now());
        reservationCreateRequest.setHour(1);

        ReservationMenuCreateRequest menuCreateRequest1 = new ReservationMenuCreateRequest();
        menuCreateRequest1.setMenuId(10L);
        menuCreateRequest1.setQuantity(1);

        ReservationMenuCreateRequest menuCreateRequest2 = new ReservationMenuCreateRequest();
        menuCreateRequest2.setMenuId(20L);
        menuCreateRequest2.setQuantity(2);

        reservationCreateRequest.setMenus(List.of(menuCreateRequest1, menuCreateRequest2));

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(storeRepository.existsById(1L)).thenReturn(true);
        Mockito.when(reservationRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Store storeMock = Mockito.mock();
        Mockito.when(storeMock.getId()).thenReturn(1L);

        Mockito.when(storeRepository.getReferenceById(1L)).thenReturn(storeMock);

        Menu menuMock1 = Mockito.mock();
        Mockito.when(menuMock1.getId()).thenReturn(10L);
        Mockito.when(menuMock1.getStore()).thenReturn(storeMock);

        Menu menuMock2 = Mockito.mock();
        Mockito.when(menuMock2.getId()).thenReturn(20L);
        Mockito.when(menuMock2.getStore()).thenReturn(storeMock);

        Mockito.when(menuRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(menuMock1, menuMock2));

        try (MockedConstruction<Reservation> ignored = Mockito.mockConstruction(
                Reservation.class,
                (mock, context) -> {
                    Mockito.when(mock.getId()).thenReturn(1L);
                    Mockito.when(mock.getStore()).thenReturn((Store) context.arguments().get(1));
                }
        )) {
            Long result = reservationService.create(1L, reservationCreateRequest);
            assertEquals(result, 1L);
        }

        Mockito.verify(reservationMenuRepository, Mockito.times(1)).saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("Testing retrieval of reservation information")
    void testReservationInfoRetrieval() {
        ReservationInfoResponse reservationInfoResponse =
                new ReservationInfoResponse(1L, 1L, "registrant", "username", LocalDate.now(), 1);
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
        ReservationInfoResponse reservationInfo1 =
                new ReservationInfoResponse(1L, 1L, "registrant", "username", LocalDate.now(), 1);
        ReservationInfoResponse reservationInfo2 =
                new ReservationInfoResponse(1L, 1L, "registrant", "username", LocalDate.now(), 2);
        ReservationInfoResponse reservationInfo3 =
                new ReservationInfoResponse(1L, 1L, "registrant", "username", LocalDate.now(), 3);

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
        LocalDate now = LocalDate.now();
        LocalDate newDate = now.plusDays(1);

        ReservationUpdateRequest request = Mockito.mock(ReservationUpdateRequest.class);
        Mockito.when(request.getDate()).thenReturn(newDate);
        Mockito.when(request.getHour()).thenReturn(10);

        Reservation reservation = new Reservation(Mockito.mock(), Mockito.mock(), now, 1);
        Mockito.when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationService.update(1L, 1L, request);

        assertEquals(newDate, reservation.getDate());
        assertEquals(10, reservation.getHour());
    }

    @Test
    @DisplayName("Testing reservation deletion functionality")
    void testReservationDeletion() {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));

        reservationService.cancel(1L, 1L);

        Mockito.verify(reservation, Mockito.times(1)).cancel();
    }

}