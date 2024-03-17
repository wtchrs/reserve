package reserve.reservation.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import reserve.notification.service.NotificationService;
import reserve.reservation.service.ReservationManageService;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationManageController.class)
@Import(JwtProvider.class)
class ReservationManageControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    ReservationManageService reservationManageService;

    @MockBean
    NotificationService notificationService;

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/cancel endpoint")
    void testCancelEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/cancel";

        Long userId = 1L;
        Long reservationId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        mockMvc.perform(
                post(urlTemplate, reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(reservationManageService, Mockito.times(1)).cancel(userId, reservationId);
        Mockito.verify(notificationService, Mockito.times(1)).notifyReservation(
                reservationId,
                "Reservation has been cancelled.",
                "Customer has cancelled the reservation."
        );
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/start endpoint")
    void testStartServiceEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/start";

        Long userId = 1L;
        Long reservationId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        mockMvc.perform(
                post(urlTemplate, reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(reservationManageService, Mockito.times(1)).startService(userId, reservationId);
        Mockito.verify(notificationService, Mockito.times(1))
                .notifyReservation(reservationId, "Service has been started.");
    }

    @Test
    @DisplayName("Testing POST /v1/reservations/manage/{reservationId}/complete endpoint")
    void testCompleteEndpoint() throws Exception {
        final String urlTemplate = "/v1/reservations/manage/{reservationId}/complete";

        Long userId = 1L;
        Long reservationId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(userId));

        mockMvc.perform(
                post(urlTemplate, reservationId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(reservationManageService, Mockito.times(1)).complete(userId, reservationId);
        Mockito.verify(notificationService, Mockito.times(1))
                .notifyReservation(reservationId, "Service has been completed.");
    }

}
