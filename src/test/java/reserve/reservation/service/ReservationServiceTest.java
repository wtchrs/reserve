package reserve.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reserve.reservation.domain.Reservation;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.infrastructure.ReservationQueryRepository;
import reserve.reservation.infrastructure.ReservationRepository;
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
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReservationService reservationService;

    @Test
    void create() {
        ReservationCreateRequest reservationCreateRequest = Mockito.mock(ReservationCreateRequest.class);
        Mockito.when(reservationCreateRequest.getStoreId()).thenReturn(1L);
        Mockito.when(reservationCreateRequest.getDate()).thenReturn(LocalDate.now());
        Mockito.when(reservationCreateRequest.getHour()).thenReturn(1);

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(storeRepository.existsById(1L)).thenReturn(true);
        Mockito.when(reservationRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedConstruction<Reservation> ignored = Mockito.mockConstruction(
                Reservation.class,
                (mock, context) -> Mockito.when(mock.getId()).thenReturn(1L)
        )) {
            Long result = reservationService.create(1L, reservationCreateRequest);
            assertEquals(result, 1L);
        }
    }

    @Test
    void getReservationInfo() {
        ReservationInfoResponse reservationInfoResponse =
                new ReservationInfoResponse(1L, 1L, "registrant", "username", LocalDate.now(), 1);
        Mockito.when(reservationRepository.findResponseByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(reservationInfoResponse));

        ReservationInfoResponse response = reservationService.getReservationInfo(1L, 1L);

        assertEquals(reservationInfoResponse, response);
    }

    @Test
    void search() {
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
    void update() {
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
    void delete() {
        Mockito.when(reservationQueryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        reservationService.delete(1L, 1L);

        Mockito.verify(reservationRepository).deleteById(1L);
    }

}