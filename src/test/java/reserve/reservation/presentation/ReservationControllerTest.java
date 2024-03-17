package reserve.reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reserve.global.BaseRestAssuredTest;
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.notification.infrastructure.NotificationRepository;
import reserve.reservation.domain.Reservation;
import reserve.reservation.domain.ReservationMenu;
import reserve.reservation.domain.ReservationStatusType;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationSearchRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.infrastructure.ReservationMenuRepository;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

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

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        String payload = objectMapper.writeValueAsString(reservationCreateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        requestFields(
                                fieldWithPath("storeId").description("The store id to make a reservation"),
                                fieldWithPath("date").description("The date to make a reservation"),
                                fieldWithPath("hour").description("The hour to make a reservation"),
                                fieldWithPath("menus[].menuId").description("The menu id to make a reservation"),
                                fieldWithPath("menus[].quantity")
                                        .description("The quantity of the menu to make a reservation")
                        ),
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
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId} endpoint")
    void testGetReservationInfoEndpoint() {
        Reservation reservation = reservationRepository.save(new Reservation(
                user1,
                store2,
                LocalDate.now().plusDays(7),
                12
        ));

        // When called by person that made the reservation
        SignInToken signInToken1 = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("reservationId").description("The id of the reservation"),
                                fieldWithPath("storeId").description("The store id of the reservation"),
                                fieldWithPath("registrant").description("The registrant of the reservation"),
                                fieldWithPath("reservationName")
                                        .description("The username of the person that made the reservation"),
                                fieldWithPath("date").description("The date of the reservation"),
                                fieldWithPath("hour").description("The hour of the reservation")
                        )
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(200)
                .body("storeId", equalTo(store2.getId().intValue()))
                .body("date", equalTo(LocalDate.now().plusDays(7).toString()))
                .body("hour", equalTo(12));

        // When called by store registrant
        SignInToken signInToken2 = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("reservationId").description("The id of the reservation"),
                                fieldWithPath("storeId").description("The store id of the reservation"),
                                fieldWithPath("registrant").description("The registrant of the reservation"),
                                fieldWithPath("reservationName")
                                        .description("The username of the person that made the reservation"),
                                fieldWithPath("date").description("The date of the reservation"),
                                fieldWithPath("hour").description("The hour of the reservation")
                        )
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(200);

        // When called by other person
        SignInToken signInToken3 = jwtProvider.generateSignInToken(String.valueOf(user3.getId()));
        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken3.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to retrieve")
                        )
                ))
                .when().get("/v1/reservations/{reservationId}", reservation.getId())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/reservations/{reservationId}/menus endpoint")
    void testGetReservationMenusEndpoint() {
        Reservation reservation =
                reservationRepository.save(new Reservation(user1, store2, LocalDate.now().plusDays(7), 12));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName1", 10000, 2));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName2", 5000, 1));
        reservationMenuRepository.save(new ReservationMenu(reservation, "menuName3", 20000, 1));

        // When called by person that made the reservation
        SignInToken signInToken1 = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken1.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("count").description("The number of menus"),
                                fieldWithPath("results[].reservationMenuId")
                                        .description("The id of the reservation menu"),
                                fieldWithPath("results[].name").description("The name of the menu"),
                                fieldWithPath("results[].price").description("The price of the menu"),
                                fieldWithPath("results[].quantity").description("The quantity of the menu")
                        )
                ))
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(200)
                .body(
                        "count", equalTo(3),
                        "results[0].name", equalTo("menuName1"),
                        "results[0].price", equalTo(10000),
                        "results[0].quantity", equalTo(2),
                        "results[1].name", equalTo("menuName2"),
                        "results[1].price", equalTo(5000),
                        "results[1].quantity", equalTo(1),
                        "results[2].name", equalTo("menuName3"),
                        "results[2].price", equalTo(20000),
                        "results[2].quantity", equalTo(1)
                );

        // When called by store registrant
        SignInToken signInToken2 = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken2.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("count").description("The number of menus"),
                                fieldWithPath("results[].reservationMenuId")
                                        .description("The id of the reservation menu"),
                                fieldWithPath("results[].name").description("The name of the menu"),
                                fieldWithPath("results[].price").description("The price of the menu"),
                                fieldWithPath("results[].quantity").description("The quantity of the menu")
                        )
                ))
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(200)
                .body("count", equalTo(3));

        // When called by other person
        SignInToken signInToken3 = jwtProvider.generateSignInToken(String.valueOf(user3.getId()));
        RestAssured
                .given(spec).header("Authorization", "Bearer " + signInToken3.getAccessToken())
                .relaxedHTTPSValidation()
                .when().get("/v1/reservations/{reservationId}/menus", reservation.getId())
                .then()
                .statusCode(403);
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

        SignInToken signInToken1 = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));
        SignInToken signInToken2 = jwtProvider.generateSignInToken(String.valueOf(user2.getId()));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                .param("query", "pasta")
                .param("date", LocalDate.now().plusDays(7).toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        queryParameters(
                                parameterWithName("type").description("The type of the search").optional(),
                                parameterWithName("query").description("The query to search").optional(),
                                parameterWithName("date").description("The date to search").optional()
                        ),
                        responseFields(
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
                        )
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body(
                        "count", equalTo(3),
                        "results[].storeId", everyItem(equalTo(store1.getId().intValue())),
                        "results[].date", everyItem(equalTo(LocalDate.now().plusDays(7).toString())),
                        "results[0].hour", equalTo(12),
                        "results[1].hour", equalTo(13),
                        "results[2].hour", equalTo(20)
                );

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        queryParameters(
                                parameterWithName("type").description("The type of the search").optional(),
                                parameterWithName("query").description("The query to search").optional(),
                                parameterWithName("date").description("The date to search").optional()
                        ),
                        responseFields(
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
                        )
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body("count", equalTo(7));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.CUSTOMER.toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        queryParameters(
                                parameterWithName("type").description("The type of the search").optional(),
                                parameterWithName("query").description("The query to search").optional(),
                                parameterWithName("date").description("The date to search").optional()
                        ),
                        responseFields(
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
                        )
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body("count", equalTo(2));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken1.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.REGISTRANT.toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        queryParameters(
                                parameterWithName("type").description("The type of the search").optional(),
                                parameterWithName("query").description("The query to search").optional(),
                                parameterWithName("date").description("The date to search").optional()
                        ),
                        responseFields(
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
                        )
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body("count", equalTo(5));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken2.getAccessToken())
                .param("type", ReservationSearchRequest.SearchType.REGISTRANT.toString())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        queryParameters(
                                parameterWithName("type").description("The type of the search").optional(),
                                parameterWithName("query").description("The query to search").optional(),
                                parameterWithName("date").description("The date to search").optional()
                        ),
                        responseFields(
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
                        )
                ))
                .when().get("/v1/reservations")
                .then()
                .statusCode(200)
                .body("count", equalTo(4));
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

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        String payload = objectMapper.writeValueAsString(reservationUpdateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
                        pathParameters(
                                parameterWithName("reservationId").description("The id of the reservation to update")
                        ),
                        requestFields(
                                fieldWithPath("date").description("The new date of the reservation"),
                                fieldWithPath("hour").description("The new hour of the reservation")
                        )
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

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user1.getId()));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        requestHeaders(
                                headerWithName("Authorization").description("The access token in bearer token format")
                        ),
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

}
