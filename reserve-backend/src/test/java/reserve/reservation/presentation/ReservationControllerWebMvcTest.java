package reserve.reservation.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import reserve.global.TestUtils;
import reserve.notification.service.NotificationService;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.dto.response.ReservationInfoListResponse;
import reserve.reservation.dto.response.ReservationInfoResponse;
import reserve.reservation.dto.response.ReservationMenuListResponse;
import reserve.reservation.dto.response.ReservationMenuResponse;
import reserve.reservation.service.ReservationService;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(JwtProvider.class)
class ReservationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    ReservationService reservationService;

    @MockBean
    NotificationService notificationService;

    @Test
    @DisplayName("Testing POST /v1/reservations endpoint")
    void testCreateEndpoint() throws Exception {
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(1L);
        reservationCreateRequest.setDate(LocalDate.now().plusDays(7));
        reservationCreateRequest.setHour(12);

        Long userId = 1L;
        Long expectedReservationId = 10L;

        Mockito.when(reservationService.create(
                Mockito.eq(userId),
                Mockito.argThat(
                        arg -> arg.getStoreId().equals(1L) &&
                               arg.getDate().equals(LocalDate.now().plusDays(7)) &&
                               arg.getHour() == 12
                )
        )).thenReturn(expectedReservationId);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                post("/v1/reservations")
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reservationCreateRequest))
        ).andExpectAll(
                status().isCreated(),
                header().string("Location", "/v1/reservations/" + expectedReservationId)
        );

        Mockito.verify(reservationService, Mockito.times(1)).create(
                Mockito.eq(userId),
                Mockito.argThat(
                        arg -> arg.getStoreId().equals(1L) &&
                               arg.getDate().equals(LocalDate.now().plusDays(7)) &&
                               arg.getHour() == 12
                )
        );
        Mockito.verify(notificationService, Mockito.times(1)).notifyReservation(
                expectedReservationId,
                "Reservation has been created.",
                "New customer has made a reservation."
        );
    }

    @Test
    @DisplayName("Testing GET /v1/reservations/{reservationId} endpoint")
    void testGetReservationInfoEndpoint() throws Exception {
        Long userId = 1L;
        Long storeId = 10L;
        Long reservationId = 100L;

        ReservationInfoResponse expectedResponse = new ReservationInfoResponse(
                reservationId,
                storeId,
                "registrant",
                "username",
                LocalDate.now().plusDays(7),
                12
        );

        Mockito.when(reservationService.getReservationInfo(userId, reservationId)).thenReturn(expectedResponse);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                get("/v1/reservations/{reservationId}", reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.storeId").value(storeId),
                jsonPath("$.date").value(LocalDate.now().plusDays(7).toString()),
                jsonPath("$.hour").value(12)
        );

        Mockito.verify(reservationService, Mockito.times(1)).getReservationInfo(userId, reservationId);
    }

    @Test
    @DisplayName("Testing GET /v1/reservations/{reservationId}/menus endpoint")
    void testGetReservationMenusEndpoint() throws Exception {
        Long userId = 1L;
        Long reservationId = 100L;

        ReservationMenuResponse menu1 = getReservationMenuResponse(1000L, "menuName1", 10000, 2);
        ReservationMenuResponse menu2 = getReservationMenuResponse(2000L, "menuName2", 5000, 1);
        ReservationMenuResponse menu3 = getReservationMenuResponse(3000L, "menuName3", 20000, 1);

        ReservationMenuListResponse expectedResponse = ReservationMenuListResponse.from(List.of(menu1, menu2, menu3));

        Mockito.when(reservationService.getReservationMenus(userId, reservationId)).thenReturn(expectedResponse);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                get("/v1/reservations/{reservationId}/menus", reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.count").value(3),
                jsonPath("$.results[0].name").value("menuName1"),
                jsonPath("$.results[0].price").value(10000),
                jsonPath("$.results[0].quantity").value(2),
                jsonPath("$.results[1].name").value("menuName2"),
                jsonPath("$.results[1].price").value(5000),
                jsonPath("$.results[1].quantity").value(1),
                jsonPath("$.results[2].name").value("menuName3"),
                jsonPath("$.results[2].price").value(20000),
                jsonPath("$.results[2].quantity").value(1)
        );
    }

    private static ReservationMenuResponse getReservationMenuResponse(
            Long reservationMenuId,
            String name,
            int price,
            int quantity
    ) {
        return new ReservationMenuResponse() {
            @Override
            public Long getReservationMenuId() {
                return reservationMenuId;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getPrice() {
                return price;
            }

            @Override
            public int getQuantity() {
                return quantity;
            }
        };
    }

    @Test
    @DisplayName("Testing GET /v1/reservations endpoint")
    void testSearchEndpoint() throws Exception {
        Long userId = 1L;

        ReservationInfoResponse reservation1 =
                new ReservationInfoResponse(1L, 1L, "user1", "store1", LocalDate.now().plusDays(7), 12);
        ReservationInfoResponse reservation2 =
                new ReservationInfoResponse(2L, 2L, "user2", "store2", LocalDate.now().plusDays(7), 13);
        ReservationInfoResponse reservation3 =
                new ReservationInfoResponse(3L, 3L, "user3", "store3", LocalDate.now().plusDays(7), 14);

        List<ReservationInfoResponse> expectedResponse = List.of(reservation1, reservation2, reservation3);

        Mockito.when(reservationService.search(Mockito.eq(userId), Mockito.any(), Mockito.eq(PageRequest.of(0, 20))))
                .thenReturn(ReservationInfoListResponse.from(
                        new PageImpl<>(expectedResponse, PageRequest.of(0, 20), 3))
                );

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                get("/v1/reservations")
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                        .param("query", "pasta")
                        .param("date", LocalDate.now().plusDays(7).toString())
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.count").value(3),
                jsonPath("$.results[0].storeId").value(1L),
                jsonPath("$.results[0].date").value(LocalDate.now().plusDays(7).toString()),
                jsonPath("$.results[0].hour").value(12),
                jsonPath("$.results[1].storeId").value(2L),
                jsonPath("$.results[1].date").value(LocalDate.now().plusDays(7).toString()),
                jsonPath("$.results[1].hour").value(13),
                jsonPath("$.results[2].storeId").value(3L),
                jsonPath("$.results[2].date").value(LocalDate.now().plusDays(7).toString()),
                jsonPath("$.results[2].hour").value(14)
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/reservations/{reservationId} endpoint")
    void testUpdateEndpoint() throws Exception {
        Long userId = 1L;
        Long reservationId = 1L;

        ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequest();
        reservationUpdateRequest.setDate(LocalDate.now().plusDays(14));
        reservationUpdateRequest.setHour(14);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                put("/v1/reservations/{reservationId}", reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reservationUpdateRequest))
        ).andExpect(status().isOk());

        Mockito.verify(reservationService, Mockito.times(1)).update(
                Mockito.eq(userId),
                Mockito.eq(reservationId),
                Mockito.argThat(arg -> arg.getDate().equals(LocalDate.now().plusDays(14)) && arg.getHour() == 14)
        );
        Mockito.verify(notificationService, Mockito.times(1)).notifyReservation(
                Mockito.eq(reservationId),
                Mockito.eq("Reservation has been updated."),
                Mockito.eq("Customer has updated the reservation.")
        );
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/{reservationId}/cancel endpoint")
    void testCancelEndpoint() throws Exception {
        Long userId = 1L;
        Long reservationId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                post("/v1/reservations/{reservationId}/cancel", reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(reservationService, Mockito.times(1)).cancel(Mockito.eq(userId), Mockito.eq(reservationId));
        Mockito.verify(notificationService, Mockito.times(1)).notifyReservation(
                Mockito.eq(reservationId),
                Mockito.eq("Reservation has been canceled."),
                Mockito.eq("Customer has canceled the reservation.")
        );
    }

}
