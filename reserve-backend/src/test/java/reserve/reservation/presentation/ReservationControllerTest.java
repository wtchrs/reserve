package reserve.reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import reserve.global.BaseRestAssuredTest;
import reserve.global.TestUtils;
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationStatusType;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.infrastructure.ReservationMenuRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.domain.TokenDetails;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static reserve.global.DocumentationSnippetUtils.*;

class ReservationControllerTest extends BaseRestAssuredTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuRepository reservationMenuRepository;

    @Autowired
    NotificationRepository notificationRepository;

    User user1, user2, user3;
    Store store1, store2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(new User("user1", "password", "hello", "ReservationControllerTest.setUp()"));
        user2 = userRepository.save(new User("user2", "password", "world", "ReservationControllerTest.setUp()"));
        user3 = userRepository.save(new User("user3", "password", "foo", "ReservationControllerTest.setUp()"));
        store1 = storeRepository.save(new Store(user1, "Pasta", "address", "description"));
        store2 = storeRepository.save(new Store(user2, "Pizza", "address", "description"));
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        reservationMenuRepository.deleteAll();
        reservationRepository.deleteAll();
        menuRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/reservations endpoint")
    void testCreateEndpoint() throws JsonProcessingException {
        Menu menu1 = menuRepository.save(new Menu(store2, "Gorgonzola", 10000, "Gorgonzola pizza"));
        Menu menu2 = menuRepository.save(new Menu(store2, "Margherita", 8000, "Margherita pizza"));

        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest();
        reservationCreateRequest.setStoreId(store2.getId());
        reservationCreateRequest.setDate(LocalDate.now().plusDays(7));
        reservationCreateRequest.setHour(12);

        ReservationMenuCreateRequest reservationMenuCreateRequest1 = new ReservationMenuCreateRequest();
        ReservationMenuCreateRequest reservationMenuCreateRequest2 = new ReservationMenuCreateRequest();
        reservationMenuCreateRequest1.setMenuId(menu1.getId());
        reservationMenuCreateRequest1.setQuantity(2);
        reservationMenuCreateRequest2.setMenuId(menu2.getId());
        reservationMenuCreateRequest2.setQuantity(1);

        reservationCreateRequest.setMenus(List.of(reservationMenuCreateRequest1, reservationMenuCreateRequest2));

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user1));

        String payload = objectMapper.writeValueAsString(reservationCreateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationCreateRequestFieldsSnippet(),
                        responseHeaders(headerWithName("Location").description("The url of the created reservation"))
                ))
                .when().post("/v1/reservations")
                .then()
                .statusCode(201)
                .header("Location", Matchers.startsWith("/v1/reservations/"));

        assertEquals(1, reservationRepository.count());
        assertEquals(2, reservationMenuRepository.count());
    }

    @Test
    @DisplayName("[Integration] Testing PUT /v1/reservations/{reservationId} endpoint")
    void testUpdateEndpoint() throws JsonProcessingException {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequest();
        reservationUpdateRequest.setDate(LocalDate.now().plusDays(14));
        reservationUpdateRequest.setHour(14);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user1));

        String payload = objectMapper.writeValueAsString(reservationUpdateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        pathParameters(
                                parameterWithName("reservationId").description("The ID of the reservation to update")
                        ),
                        reservationUpdateRequestFieldsSnippet()
                ))
                .when().put("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(200);

        reservationRepository.findById(reservation.getId()).ifPresentOrElse(
                updatedReservation -> {
                    assertEquals(LocalDate.now().plusDays(14), updatedReservation.getDate());
                    assertEquals(14, updatedReservation.getHour());
                },
                () -> fail("Reservation not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/reservations/{reservationId}/cancel endpoint")
    void testCancelEndpoint() {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user1));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to cancel")
                        )
                ))
                .when().post("/v1/reservations/{reservationId}/cancel", reservation.getId())
                .then()
                .statusCode(200);

        reservationRepository.findById(reservation.getId()).ifPresentOrElse(
                updatedReservation -> assertEquals(ReservationStatusType.CANCELLED, updatedReservation.getStatus()),
                () -> fail("Reservation not found")
        );
    }

    /**
     * @return The request fields snippet
     * @see ReservationCreateRequest
     */
    private static RequestFieldsSnippet reservationCreateRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("storeId").description("The store id to make a reservation"),
                fieldWithPath("date").description("The date to make a reservation"),
                fieldWithPath("hour").description("The hour to make a reservation"),
                fieldWithPath("menus[].menuId").description("The menu id to make a reservation"),
                fieldWithPath("menus[].quantity").description("The quantity of the menu to make a reservation")
        );
    }

    /**
     * @return The request fields snippet
     * @see ReservationUpdateRequest
     */
    private static RequestFieldsSnippet reservationUpdateRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("date").description("The new date of the reservation"),
                fieldWithPath("hour").description("The new hour of the reservation")
        );
    }

}
