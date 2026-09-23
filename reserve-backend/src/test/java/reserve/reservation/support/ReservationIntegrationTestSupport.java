package reserve.reservation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reserve.menu.domain.Menu;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.support.BaseRestAssuredTest;
import reserve.support.TestUtils;
import reserve.user.domain.User;

import java.time.LocalDate;
import java.util.List;

public abstract class ReservationIntegrationTestSupport extends BaseRestAssuredTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtProvider jwtProvider;

    protected ReservationCreateRequest reservationCreateRequest(Menu menu, Store store) {
        return reservationCreateRequest(menu, store, LocalDate.of(2026, 1, 1), 12);
    }

    protected ReservationCreateRequest reservationCreateRequest(Menu menu, Store store, LocalDate date, int hour) {
        ReservationMenuCreateRequest reservationMenuCreateRequest = new ReservationMenuCreateRequest();
        reservationMenuCreateRequest.setMenuId(menu.getId());
        reservationMenuCreateRequest.setQuantity(1);

        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(store.getId());
        reservationCreateRequest.setDate(date);
        reservationCreateRequest.setHour(hour);
        reservationCreateRequest.setMenus(List.of(reservationMenuCreateRequest));
        return reservationCreateRequest;
    }

    protected ReservationUpdateRequest reservationUpdateRequest() {
        return reservationUpdateRequest(LocalDate.of(2026, 1, 2), 14);
    }

    protected ReservationUpdateRequest reservationUpdateRequest(LocalDate date, int hour) {
        ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequest();
        reservationUpdateRequest.setDate(date);
        reservationUpdateRequest.setHour(hour);
        return reservationUpdateRequest;
    }

    protected RequestSpecification authenticatedRequest(User user) {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));
        return RestAssured.given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken());
    }

    protected Response createReservation(User user, ReservationCreateRequest request) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(request);
        return authenticatedRequest(user).contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .post("/v1/reservations");
    }

    protected Response updateReservation(User user, Long reservationId, ReservationUpdateRequest request)
            throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(request);
        return authenticatedRequest(user).contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .put("/v1/reservations/{reservationId}", reservationId);
    }

    protected Response cancelReservation(User user, Long reservationId) {
        return authenticatedRequest(user).when().post("/v1/reservations/{reservationId}/cancel", reservationId);
    }

    protected Response cancelReservationAsRegistrant(User registrant, Long reservationId) {
        return authenticatedRequest(registrant).when()
            .post("/v1/reservations/manage/{reservationId}/cancel", reservationId);
    }

    protected Response startServiceReservationAsRegistrant(User registrant, Long reservationId) {
        return authenticatedRequest(registrant).when()
            .post("/v1/reservations/manage/{reservationId}/start", reservationId);
    }

    protected Response completeReservationAsRegistrant(User registrant, Long reservationId) {
        return authenticatedRequest(registrant).when()
            .post("/v1/reservations/manage/{reservationId}/complete", reservationId);
    }

}
