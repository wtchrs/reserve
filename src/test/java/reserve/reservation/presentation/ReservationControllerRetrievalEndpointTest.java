package reserve.reservation.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.QueryParametersSnippet;
import reserve.global.BaseRestAssuredTest;
import reserve.global.exception.ErrorCode;
import reserve.menu.infrastructure.MenuRepository;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationMenu;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.infrastructure.ReservationMenuRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static reserve.global.DocumentationSnippetUtils.*;

class ReservationControllerRetrievalEndpointTest extends BaseRestAssuredTest {

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
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId} endpoint for reservation maker")
    void testGetReservationInfoEndpointForReservationMaker() {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        // When called by person that made the reservation
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        reservationInfoResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(200)
                .body("storeId", equalTo(store2.getId().intValue()))
                .body("date", equalTo(LocalDate.now().plusDays(7).toString()))
                .body("hour", equalTo(12));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId} endpoint for store registrant")
    void testGetReservationInfoEndpointForStoreRegistrant() {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        // When called by store registrant
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        reservationInfoResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("[Integration][Fail] Testing GET /v1/reservations/{reservationId} endpoint for other users")
    void testGetReservationInfoEndpointForOtherUsers() {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        // When called by other users
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user3.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(404)
                .body("errorCode", equalTo(ErrorCode.RESERVATION_NOT_FOUND.getCode()))
                .body("message", equalTo(ErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId}/menus endpoint for reservation maker")
    void testGetReservationMenusEndpointForReservationMaker() {
        Reservation reservation =
                reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 12));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName1", 10000, 2));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName2", 5000, 1));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName3", 20000, 1));

        // When called by person that made the reservation
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        reservationMenuListResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(200)
                .body("count", equalTo(3))
                .body("results[0].name", equalTo("menuName1"))
                .body("results[0].price", equalTo(10000))
                .body("results[0].quantity", equalTo(2))
                .body("results[1].name", equalTo("menuName2"))
                .body("results[1].price", equalTo(5000))
                .body("results[1].quantity", equalTo(1))
                .body("results[2].name", equalTo("menuName3"))
                .body("results[2].price", equalTo(20000))
                .body("results[2].quantity", equalTo(1));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId}/menus endpoint for store registrant")
    void testGetReservationMenusEndpointForStoreRegistrant() {
        Reservation reservation =
                reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 12));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName1", 10000, 2));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName2", 5000, 1));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName3", 20000, 1));

        // When called by store registrant
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        reservationMenuListResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(200)
                .body("count", equalTo(3));
    }

    @Test
    @DisplayName("[Integration][Fail] Testing GET /v1/reservations/{reservationId}/menus endpoint for other users")
    void testGetReservationMenusEndpointForOtherUsers() {
        Reservation reservation =
                reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 12));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName1", 10000, 2));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName2", 5000, 1));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName3", 20000, 1));

        // When called by other users
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user3.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationIdPathParametersSnippet(),
                        errorResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(403)
                .body("errorCode", equalTo(ErrorCode.ACCESS_DENIED.getCode()))
                .body("message", equalTo(ErrorCode.ACCESS_DENIED.getMessage()));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/reservations endpoint")
    void testSearchEndpoint() {
        reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 12));
        reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 13));
        reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(7), 20));
        reservationRepository.save(new Reservation(user1, store1, LocalDate.now().plusDays(8), 12));
        reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 14));
        reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 15));
        reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(8), 12));
        reservationRepository.save(new Reservation(user2, store1, LocalDate.now().plusDays(7), 12));
        reservationRepository.save(new Reservation(user2, store2, LocalDate.now().plusDays(7), 13));

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                .param("query", "pasta")
                .param("date", LocalDate.now().plusDays(7).toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        reservationSearchRequestQueryParametersSnippet(),
                        reservationInfoListResponseFieldsSnippet()
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body("count", equalTo(3))
                .body("results[].storeId", everyItem(equalTo(store1.getId().intValue())))
                .body("results[].date", everyItem(equalTo(LocalDate.now().plusDays(7).toString())))
                .body("results[0].hour", equalTo(12))
                .body("results[1].hour", equalTo(13))
                .body("results[2].hour", equalTo(20));
    }

    private static PathParametersSnippet reservationIdPathParametersSnippet() {
        return pathParameters(parameterWithName("reservationId").description("The ID of the reservation to retrieve"));
    }

    /**
     * @return the query parameters snippet
     * @see reserve.reservation.dto.request.ReservationSearchRequest
     */
    private static QueryParametersSnippet reservationSearchRequestQueryParametersSnippet() {
        return queryParameters(
                parameterWithName("type").description("The type of the search").optional(),
                parameterWithName("query").description("The query to search").optional(),
                parameterWithName("date").description("The date to search").optional()
        );
    }

    /**
     * @return the response fields snippet
     * @see reserve.reservation.dto.response.ReservationInfoResponse
     */
    private static ResponseFieldsSnippet reservationInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("reservationId").description("The id of the reservation"),
                fieldWithPath("storeId").description("The store id of the reservation"),
                fieldWithPath("registrant").description("The registrant of the reservation"),
                fieldWithPath("reservationName").description("The username of the person that made the reservation"),
                fieldWithPath("date").description("The date of the reservation"),
                fieldWithPath("hour").description("The hour of the reservation")
        );
    }

    /**
     * @return the response fields snippet
     * @see reserve.reservation.dto.response.ReservationMenuListResponse
     */
    private static ResponseFieldsSnippet reservationMenuListResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("count").description("The number of menus"),
                fieldWithPath("results[].reservationMenuId").description("The id of the reservation menu"),
                fieldWithPath("results[].name").description("The name of the menu"),
                fieldWithPath("results[].price").description("The price of the menu"),
                fieldWithPath("results[].quantity").description("The quantity of the menu")
        );
    }

    /**
     * @return the response fields snippet
     * @see reserve.reservation.dto.response.ReservationInfoListResponse
     */
    private static ResponseFieldsSnippet reservationInfoListResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("count").description("The number of reservations"),
                fieldWithPath("pageNumber")
                        .description("The page number of the search. The page number starts with 0."),
                fieldWithPath("pageSize").description("The page size of the search"),
                fieldWithPath("hasNext").description("Whether there is a next page"),
                fieldWithPath("results").description("The list of reservations"),
                fieldWithPath("results[].reservationId").description("The id of the reservation"),
                fieldWithPath("results[].storeId").description("The store id of the reservation"),
                fieldWithPath("results[].registrant").description("The registrant of the reservation"),
                fieldWithPath("results[].reservationName")
                        .description("The username of the person that made the reservation"),
                fieldWithPath("results[].date").description("The date of the reservation"),
                fieldWithPath("results[].hour").description("The hour of the reservation")
        );
    }

}
