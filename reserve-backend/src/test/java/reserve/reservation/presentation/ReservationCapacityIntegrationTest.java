package reserve.reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import reserve.global.exception.ErrorCode;
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationMenuCreateRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.support.BaseRestAssuredTest;
import reserve.support.TestUtils;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ReservationCapacityIntegrationTest extends BaseRestAssuredTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    ReservationRepository reservationRepository;

    ReservationCreateRequest getReservationCreateRequest(Menu menu, Store store) {
        return getReservationCreateRequest(menu, store, LocalDate.of(2026, 1, 1), 12);
    }

    ReservationCreateRequest getReservationCreateRequest(Menu menu, Store store, LocalDate date, int hour) {
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

    ReservationUpdateRequest getReservationUpdateRequest() {
        return getReservationUpdateRequest(LocalDate.of(2026, 1, 2), 14);
    }

    ReservationUpdateRequest getReservationUpdateRequest(LocalDate date, int hour) {
        ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequest();
        reservationUpdateRequest.setDate(date);
        reservationUpdateRequest.setHour(hour);
        return reservationUpdateRequest;
    }

    int getReservationSlotCounter(Store store, LocalDate date, int hour) {
        String sql = """
                SELECT slot_count
                FROM reservation_slots
                WHERE store_id = ?
                  AND slot_date = ?
                  AND slot_hour = ?;
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("slot_count"), store.getId(), date, hour).get(0);
    }

    Response sendReservationCreateRequest(User user, ReservationCreateRequest request) throws JsonProcessingException {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));
        String payload = objectMapper.writeValueAsString(request);
        return RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .post("/v1/reservations");
    }

    Response sendReservationUpdateRequest(User user, Long reservationId, ReservationUpdateRequest request)
            throws JsonProcessingException {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));
        String payload = objectMapper.writeValueAsString(request);
        return RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .put("/v1/reservations/{reservationId}", reservationId);
    }

    Response sendReservationCancellationRequest(User user, Long reservationId) {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));
        return RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .when()
            .post("/v1/reservations/{reservationId}/cancel", reservationId);
    }

    Response sendReservationManageCancellationRequest(User registrant, Long reservationId) {
        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(registrant));
        return RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .when()
            .post("/v1/reservations/manage/{reservationId}/cancel", reservationId);
    }

    @Test
    void returnsConflict_whenSlotIsAtCapacity() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer1 = userRepository
            .save(new User("customer1", "password", "world", "ReservationControllerCapacityTest"));
        User customer2 = userRepository
            .save(new User("customer2", "password", "foo", "ReservationControllerCapacityTest"));
        // Capacity is 1.
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Succeed at first request
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        sendReservationCreateRequest(customer1, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"));

        // Fail at second request
        sendReservationCreateRequest(customer2, createRequest).then()
            .statusCode(409)
            .body("code", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getCode()))
            .body("message", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getMessage()));
    }

    @Test
    void returnsConflict_whenStoreCapacityIsZero() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 0));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(409)
            .body("code", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getCode()))
            .body("message", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getMessage()));
    }

    @Test
    void returnsCreated_whenStoreCapacityIsUnlimited() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", -1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"));
    }

    @Test
    void returnsCreated_whenPreviousReservationInSameSlotIsCanceled() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer1 = userRepository
            .save(new User("customer1", "password", "world", "ReservationControllerCapacityTest"));
        User customer2 = userRepository
            .save(new User("customer2", "password", "foo", "ReservationControllerCapacityTest"));
        // Capacity is 1.
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer1, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));

        // Cancel
        sendReservationCancellationRequest(customer1, reservationId).then().statusCode(200);

        // Succeed to create
        sendReservationCreateRequest(customer2, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
    }

    @Test
    void preservesSlotCount_whenCanceledReservationIsCanceledAgain() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", -1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));

        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        sendReservationCancellationRequest(customer, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel already canceled reservation
        sendReservationCancellationRequest(customer, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void releasesOldSlotAndAcquiresNewSlot_whenReservationIsMovedToAnotherSlot() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Update
        ReservationUpdateRequest updateRequest = getReservationUpdateRequest();
        sendReservationUpdateRequest(customer, reservationId, updateRequest).then().statusCode(200);

        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
        assertEquals(1, getReservationSlotCounter(store, updateRequest.getDate(), updateRequest.getHour()));
    }

    @Test
    void preservesReservationAndSlotCounts_whenTargetSlotIsFull() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer1 = userRepository
            .save(new User("customer1", "password", "world", "ReservationControllerCapacityTest"));
        User customer2 = userRepository
            .save(new User("customer2", "password", "foo", "ReservationControllerCapacityTest"));
        // Capacity is 1.
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest1 = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer1, createRequest1).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));

        // Create at other slot
        ReservationCreateRequest createRequest2 = getReservationCreateRequest(menu, store, LocalDate.of(2026, 1, 2),
                14);
        sendReservationCreateRequest(customer2, createRequest2).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"));

        assertEquals(1, getReservationSlotCounter(store, createRequest1.getDate(), createRequest1.getHour()));
        assertEquals(1, getReservationSlotCounter(store, createRequest2.getDate(), createRequest2.getHour()));

        // Fail to update
        ReservationUpdateRequest updateRequest = getReservationUpdateRequest();
        sendReservationUpdateRequest(customer1, reservationId, updateRequest).then()
            .statusCode(409)
            .body("code", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getCode()))
            .body("message", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getMessage()));

        assertEquals(1, getReservationSlotCounter(store, createRequest1.getDate(), createRequest1.getHour()));
        assertEquals(1, getReservationSlotCounter(store, updateRequest.getDate(), updateRequest.getHour()));

        // The reservation should be rolled back.
        reservationRepository.findByIdAndUserId(reservationId, customer1.getId()).ifPresentOrElse(reservation -> {
            assertEquals(store.getId(), reservation.getStore().getId());
            assertEquals(createRequest1.getDate(), reservation.getDate());
            assertEquals(createRequest1.getHour(), reservation.getHour());
        }, () -> fail("Reservation should exist."));
    }

    @Test
    void returnsOk_whenReservationRemainsInSameFullSlot() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Update to the same slot that is full.
        ReservationUpdateRequest updateRequest = getReservationUpdateRequest(createRequest.getDate(),
                createRequest.getHour());
        sendReservationUpdateRequest(customer, reservationId, updateRequest).then().statusCode(200);
        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void returnsOk_whenReservationIsCanceledByStore() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        sendReservationManageCancellationRequest(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void preservesSlotCount_whenCanceledReservationIsCanceledAgainByStore() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);
        String locationHeader = sendReservationCreateRequest(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
        assertEquals(1, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        sendReservationManageCancellationRequest(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel again
        sendReservationManageCancellationRequest(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void createsReservationsUpToCapacity_whenRequestsAreConcurrent() {
        final int numRequests = 10;
        final int slotCapacity = 3;

        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", slotCapacity));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        ReservationCreateRequest createRequest = getReservationCreateRequest(menu, store);

        ExecutorService pool = Executors.newFixedThreadPool(numRequests);
        try {
            CyclicBarrier barrier = new CyclicBarrier(numRequests);

            List<Future<Boolean>> futures = IntStream.range(0, numRequests).mapToObj(i -> pool.submit(() -> {
                barrier.await();
                try {
                    return sendReservationCreateRequest(customer, createRequest).then().extract().statusCode() == 201;
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })).toList();

            long numSucceeded = futures.stream().map(f -> {
                try {
                    return f.get(10, TimeUnit.SECONDS);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                catch (ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }).filter(r -> r).count();

            assertEquals(slotCapacity, numSucceeded);
        }
        finally {
            pool.shutdown();
        }

        assertEquals(slotCapacity, getReservationSlotCounter(store, createRequest.getDate(), createRequest.getHour()));
    }

}
