package reserve.reservation.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationStatusType;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ReservationControllerTest {

    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    User user1, user2, user3;
    Store store1, store2;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        user1 = userRepository.save(new User("user1", "password", "hello", "description"));
        user2 = userRepository.save(new User("user2", "password", "world", "description"));
        user3 = userRepository.save(new User("user3", "password", "foo", "description"));
        store1 = storeRepository.save(new Store(user1, "Pasta", 10000, "address", "description"));
        store2 = storeRepository.save(new Store(user2, "Pizza", 20000, "address", "description"));
    }

    @Test
    @DisplayName("Testing POST /v1/reservations endpoint")
    void testCreateEndpoint() throws Exception {
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(store2.getId());
        reservationCreateRequest.setDate(LocalDate.now().plusDays(7));
        reservationCreateRequest.setHour(12);

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        mockMvc.perform(
                        post("/v1/reservations")
                                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(reservationCreateRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.startsWith("/v1/reservations/")));

        assertEquals(1, reservationRepository.count());
    }

    @Test
    @DisplayName("Testing GET /v1/reservations/{reservationId} endpoint")
    void testGetReservationInfoEndpoint() throws Exception {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        SignInToken signInToken1 = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        mockMvc.perform(
                        get("/v1/reservations/{reservationId}", reservation.getId())
                                .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                )
                .andExpect(status().isOk())
                .andExpectAll(
                        content().contentType("application/json"),
                        jsonPath("$.storeId").value(store2.getId()),
                        jsonPath("$.date").value(LocalDate.now().plusDays(7).toString()),
                        jsonPath("$.hour").value(12)
                );

        SignInToken signInToken2 = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));
        mockMvc.perform(
                        get("/v1/reservations/{reservationId}", reservation.getId())
                                .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                )
                .andExpect(status().isOk())
                .andExpectAll(
                        content().contentType("application/json"),
                        jsonPath("$.storeId").value(store2.getId()),
                        jsonPath("$.date").value(LocalDate.now().plusDays(7).toString()),
                        jsonPath("$.hour").value(12)
                );

        SignInToken signInToken3 = jwtProvider.generateSignInToken(String.valueOf(user3.getId()));
        mockMvc.perform(
                        get("/v1/reservations/{reservationId}", reservation.getId())
                                .header("Authorization", "Bearer " + signInToken3.getAccessToken())
                )
                .andExpect(status().isNotFound());
    }

    @Nested
    class ReservationSearchTest {

        @BeforeEach
        @Transactional
        @Commit
        void setUp() {
            reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 12));
            reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 13));
            reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 20));
            reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(8), 12));
            reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 14));
            reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 15));
            reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(8), 12));
            reservationRepository.save(new Reservation(user2, store1, LocalDate.now().plusDays(7), 12));
            reservationRepository.save(new Reservation(user2, store2, LocalDate.now().plusDays(7), 13));
        }

        @AfterEach
        @Transactional
        @Commit
        void tearDown() {
            reservationRepository.deleteAll();
            storeRepository.deleteAll();
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("Testing GET /v1/reservations endpoint")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void testSearchEndpoint() throws Exception {
            SignInToken signInToken1 = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
            SignInToken signInToken2 = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));

            mockMvc.perform(
                    get("/v1/reservations")
                            .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                            .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                            .param("query", "pasta")
                            .param("date", LocalDate.now().plusDays(7).toString())
            ).andExpectAll(
                    status().isOk(),
                    content().contentType("application/json"),
                    jsonPath("$.count").value(3),
                    jsonPath("$.results[*].storeId", everyItem(equalTo(store1.getId().intValue()))),
                    jsonPath("$.results[*].date", everyItem(equalTo(LocalDate.now().plusDays(7).toString()))),
                    jsonPath("$.results[0].hour").value(12),
                    jsonPath("$.results[1].hour").value(13),
                    jsonPath("$.results[2].hour").value(20)
            );

            mockMvc.perform(
                    get("/v1/reservations")
                            .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                            .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
            ).andExpectAll(
                    status().isOk(),
                    content().contentType("application/json"),
                    jsonPath("$.count").value(7)
            );

            mockMvc.perform(
                    get("/v1/reservations")
                            .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                            .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
            ).andExpectAll(
                    status().isOk(),
                    content().contentType("application/json"),
                    jsonPath("$.count").value(2)
            );

            mockMvc.perform(
                    get("/v1/reservations")
                            .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                            .param("type", ReservationSearchRequest.SearchType.REGISTRANT.toString())
            ).andExpectAll(
                    status().isOk(),
                    content().contentType("application/json"),
                    jsonPath("$.count").value(5)
            );

            mockMvc.perform(
                    get("/v1/reservations")
                            .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                            .param("type", ReservationSearchRequest.SearchType.REGISTRANT.toString())
            ).andExpectAll(
                    status().isOk(),
                    content().contentType("application/json"),
                    jsonPath("$.count").value(4)
            );
        }

    }

    @Test
    @DisplayName("Testing PUT /v1/reservations/{reservationId} endpoint")
    void testUpdateEndpoint() throws Exception {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequest();
        reservationUpdateRequest.setDate(LocalDate.now().plusDays(14));
        reservationUpdateRequest.setHour(14);

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        mockMvc.perform(
                put("/v1/reservations/{reservationId}", reservation.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reservationUpdateRequest))
        ).andExpect(status().isOk());

        reservationRepository.findById(reservation.getId()).ifPresentOrElse(
                updatedReservation -> {
                    assertEquals(LocalDate.now().plusDays(14), updatedReservation.getDate());
                    assertEquals(14, updatedReservation.getHour());
                },
                () -> fail("Reservation not found")
        );
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/{reservationId}/cancel endpoint")
    void testCancelEndpoint() throws Exception {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        mockMvc.perform(
                post("/v1/reservations/{reservationId}/cancel", reservation.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        reservationRepository.findById(reservation.getId()).ifPresentOrElse(
                updatedReservation -> assertEquals(ReservationStatusType.CANCELLED, updatedReservation.getStatus()),
                () -> fail("Reservation not found")
        );
    }

}
