package reserve.reservation.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reserve.reservation.domain.Reservation;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ReservationManageControllerTest {

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

    User user, registrant;
    Store store;
    Reservation ready, inService, completed, cancelled;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        user = userRepository.save(new User("username", "password", "nickname", "description"));
        registrant = userRepository.save(new User("registrant", "password", "nickname", "description"));
        store = storeRepository.save(new Store(registrant, "store", 1000, "address", "description"));
        ready = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        inService = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        completed = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));
        cancelled = reservationRepository.save(new Reservation(user, store, LocalDate.now().plusDays(7), 12));

        inService.start();
        completed.start();
        completed.complete();
        cancelled.cancel();
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/cancel endpoint")
    void testCancelEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/cancel";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        mockMvc.perform(
                post(urlTemplate, ready.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(urlTemplate, inService.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());

        mockMvc.perform(
                post(urlTemplate, completed.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());

        mockMvc.perform(
                post(urlTemplate, cancelled.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/start endpoint")
    void testStartServiceEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/start";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        mockMvc.perform(
                post(urlTemplate, ready.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(urlTemplate, inService.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(urlTemplate, completed.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());

        mockMvc.perform(
                post(urlTemplate, cancelled.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/complete endpoint")
    void testCompleteEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/complete";

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(registrant.getId()));

        mockMvc.perform(
                post(urlTemplate, ready.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());

        mockMvc.perform(
                post(urlTemplate, inService.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(urlTemplate, completed.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(urlTemplate, cancelled.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isConflict());
    }

}
