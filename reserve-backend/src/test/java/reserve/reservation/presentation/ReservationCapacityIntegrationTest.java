package reserve.reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import reserve.global.exception.ErrorCode;
import reserve.menu.domain.Menu;
import reserve.menu.infrastructure.MenuRepository;
import reserve.reservation.dto.request.ReservationCreateRequest;
import reserve.reservation.dto.request.ReservationUpdateRequest;
import reserve.reservation.infrastructure.ReservationRepository;
import reserve.reservation.support.ReservationIntegrationTestSupport;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationCapacityIntegrationTest extends ReservationIntegrationTestSupport {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    ReservationRepository reservationRepository;

    int getReservationSlotCount(Store store, LocalDate date, int hour) {
        String sql = """
                SELECT slot_count
                FROM reservation_slots
                WHERE store_id = ?
                  AND slot_date = ?
                  AND slot_hour = ?;
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("slot_count"), store.getId(), date, hour).get(0);
    }

    private static long extractReservationId(String locationHeader) {
        return Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
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
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        createReservation(customer1, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"));

        // Fail at second request
        createReservation(customer2, createRequest).then()
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

        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        createReservation(customer, createRequest).then()
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

        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        createReservation(customer, createRequest).then()
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
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer1, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);

        // Cancel
        cancelReservation(customer1, reservationId).then().statusCode(200);

        // Succeed to create
        createReservation(customer2, createRequest).then()
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
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);

        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        cancelReservation(customer, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel already canceled reservation
        cancelReservation(customer, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void releasesOldSlotAndAcquiresNewSlot_whenReservationIsMovedToAnotherSlot() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Update
        ReservationUpdateRequest updateRequest = reservationUpdateRequest();
        updateReservation(customer, reservationId, updateRequest).then().statusCode(200);

        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
        assertEquals(1, getReservationSlotCount(store, updateRequest.getDate(), updateRequest.getHour()));
    }

    @Test
    void preservesReservationAndSlotCounts_whenUpdateTargetSlotIsFull() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer1 = userRepository
            .save(new User("customer1", "password", "world", "ReservationControllerCapacityTest"));
        User customer2 = userRepository
            .save(new User("customer2", "password", "foo", "ReservationControllerCapacityTest"));
        // Capacity is 1.
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest1 = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer1, createRequest1).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);

        // Create at other slot
        ReservationCreateRequest createRequest2 = reservationCreateRequest(menu, store, LocalDate.of(2026, 1, 2), 14);
        createReservation(customer2, createRequest2).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"));

        assertEquals(1, getReservationSlotCount(store, createRequest1.getDate(), createRequest1.getHour()));
        assertEquals(1, getReservationSlotCount(store, createRequest2.getDate(), createRequest2.getHour()));

        // Fail to update
        ReservationUpdateRequest updateRequest = reservationUpdateRequest();
        updateReservation(customer1, reservationId, updateRequest).then()
            .statusCode(409)
            .body("code", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getCode()))
            .body("message", equalTo(ErrorCode.RESERVATION_SLOT_FULL.getMessage()));

        assertEquals(1, getReservationSlotCount(store, createRequest1.getDate(), createRequest1.getHour()));
        assertEquals(1, getReservationSlotCount(store, updateRequest.getDate(), updateRequest.getHour()));

        // The reservation should be rolled back.
        reservationRepository.findByIdAndUserId(reservationId, customer1.getId()).ifPresentOrElse(reservation -> {
            assertEquals(store.getId(), reservation.getStore().getId());
            assertEquals(createRequest1.getDate(), reservation.getDate());
            assertEquals(createRequest1.getHour(), reservation.getHour());
        }, () -> fail("Reservation should exist."));
    }

    @Test
    void returnsOk_whenReservationUpdateRemainsInSameFullSlot() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Update to the same slot that is full.
        ReservationUpdateRequest updateRequest = reservationUpdateRequest(createRequest);
        updateReservation(customer, reservationId, updateRequest).then().statusCode(200);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void returnsOk_whenReservationIsCanceledByStore() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        cancelReservationAsRegistrant(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void preservesSlotCount_whenCanceledReservationIsCanceledAgainByStore() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", 1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel
        cancelReservationAsRegistrant(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel again
        cancelReservationAsRegistrant(user, reservationId).then().statusCode(200);
        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
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

        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);

        ExecutorService pool = Executors.newFixedThreadPool(numRequests);
        try {
            CyclicBarrier barrier = new CyclicBarrier(numRequests);

            List<Future<Boolean>> futures = IntStream.range(0, numRequests).mapToObj(i -> pool.submit(() -> {
                barrier.await();
                try {
                    return createReservation(customer, createRequest).then().extract().statusCode() == 201;
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

        assertEquals(slotCapacity, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
    }

    @Test
    void decreasesSlotCountOnce_whenCancellationRequestsAreConcurrent() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", -1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);

        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Cancel concurrently
        final int numRequests = 10;
        ExecutorService pool = Executors.newFixedThreadPool(numRequests);
        try {
            CyclicBarrier barrier = new CyclicBarrier(numRequests);

            List<Future<Boolean>> futures = IntStream.range(0, numRequests).mapToObj(i -> pool.submit(() -> {
                barrier.await();
                return cancelReservation(customer, reservationId).then().extract().statusCode() == 200;
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

            assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
            assertEquals(numRequests, numSucceeded);
        }
        finally {
            pool.shutdown();
        }
    }

    @Test
    void movesSlotCountOnce_whenUpdateRequestsAreConcurrent() throws JsonProcessingException {
        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", -1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest = reservationCreateRequest(menu, store);
        String locationHeader = createReservation(customer, createRequest).then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/reservations/"))
            .extract()
            .header("Location");
        long reservationId = extractReservationId(locationHeader);
        assertEquals(1, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));

        // Update concurrently
        ReservationUpdateRequest updateRequest = reservationUpdateRequest();

        final int numRequests = 10;
        ExecutorService pool = Executors.newFixedThreadPool(numRequests);
        try {
            CyclicBarrier barrier = new CyclicBarrier(numRequests);

            List<Future<Boolean>> futures = IntStream.range(0, numRequests).mapToObj(i -> pool.submit(() -> {
                barrier.await();
                return updateReservation(customer, reservationId, updateRequest).then().extract().statusCode() == 200;
            })).toList();

            futures.forEach(f -> {
                try {
                    f.get();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        finally {
            pool.shutdown();
        }

        assertEquals(0, getReservationSlotCount(store, createRequest.getDate(), createRequest.getHour()));
        assertEquals(1, getReservationSlotCount(store, updateRequest.getDate(), updateRequest.getHour()));
    }

    private long[] createReservations(User customer, ReservationCreateRequest request, int count)
            throws JsonProcessingException {
        long[] results = new long[count];
        for (int i = 0; i < count; i++) {
            String locationHeader = createReservation(customer, request).then()
                .statusCode(201)
                .header("Location", Matchers.startsWith("/v1/reservations/"))
                .extract()
                .header("Location");
            results[i] = extractReservationId(locationHeader);
        }
        return results;
    }

    @Test
    void completesUpdatesAndPreservesSlotCounts_whenReservationsMoveBetweenTwoSlotsConcurrently()
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        int numReservations = 10;
        int numTasks = numReservations * 2;

        User user = userRepository.save(new User("user", "password", "hello", "ReservationControllerCapacityTest"));
        User customer = userRepository
            .save(new User("customer", "password", "world", "ReservationControllerCapacityTest"));
        Store store = storeRepository.save(new Store(user, "store", "address", "description", -1));
        Menu menu = menuRepository.save(new Menu(store, "menu", 1000, "menu"));

        // Create
        ReservationCreateRequest createRequest1 = reservationCreateRequest(menu, store, LocalDate.of(2026, 1, 1), 12);
        long[] slot1ReservationIds = createReservations(customer, createRequest1, numReservations);

        ReservationCreateRequest createRequest2 = reservationCreateRequest(menu, store, LocalDate.of(2026, 1, 2), 14);
        long[] slot2ReservationIds = createReservations(customer, createRequest2, numReservations);

        // Update concurrently
        ReservationUpdateRequest moveToSlot1 = reservationUpdateRequest(createRequest1);
        ReservationUpdateRequest moveToSlot2 = reservationUpdateRequest(createRequest2);

        ExecutorService pool = Executors.newFixedThreadPool(numReservations * 2);
        try {
            CountDownLatch readySignal = new CountDownLatch(numTasks);
            CountDownLatch startSignal = new CountDownLatch(1);

            record ReservationMove(long[] ids, ReservationUpdateRequest request) {
            }
            List<ReservationMove> reservationMoves = List.of(new ReservationMove(slot1ReservationIds, moveToSlot2),
                    new ReservationMove(slot2ReservationIds, moveToSlot1));

            List<Future<Integer>> futures = new ArrayList<>();

            for (ReservationMove reservationMove : reservationMoves) {
                for (long reservationId : reservationMove.ids()) {
                    futures.add(pool.submit(() -> {
                        readySignal.countDown();
                        startSignal.await();
                        return updateReservation(customer, reservationId, reservationMove.request()).then()
                            .extract()
                            .statusCode();
                    }));
                }
            }

            assertTrue(readySignal.await(10, TimeUnit.SECONDS));
            startSignal.countDown();

            for (Future<Integer> future : futures) {
                assertEquals(200, future.get(10, TimeUnit.SECONDS));
            }
        }
        finally {
            pool.shutdownNow();
        }

        assertAll(
                () -> assertEquals(numReservations,
                        getReservationSlotCount(store, createRequest1.getDate(), createRequest1.getHour())),
                () -> assertEquals(numReservations,
                        getReservationSlotCount(store, createRequest2.getDate(), createRequest2.getHour())));
    }

}
