package reserve.store.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reserve.support.BaseRestAssuredTest;
import reserve.support.TestUtils;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

class StoreControllerTest extends BaseRestAssuredTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("username", "password", "nickname", "StoreControllerTest.setUp()"));
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/stores endpoint")
    void testCreateEndpoint() throws JsonProcessingException {
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest();
        storeCreateRequest.setName("Store name");
        storeCreateRequest.setAddress("City, Street, Zipcode");
        storeCreateRequest.setDescription("StoreControllerTest.testCreateEndpoint()");
        storeCreateRequest.setCapacity(5);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeCreateRequest);

        String locationHeader = RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .post("/v1/stores")
            .then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/stores/"))
            .extract()
            .header("Location");

        long createdId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));

        assertEquals(1, storeRepository.count());
        storeRepository.findByIdAndUserId(createdId, user.getId()).ifPresentOrElse(created -> {
            assertEquals(storeCreateRequest.getName(), created.getName());
            assertEquals(storeCreateRequest.getAddress(), created.getAddress());
            assertEquals(storeCreateRequest.getDescription(), created.getDescription());
            assertEquals(storeCreateRequest.getCapacity(), created.getCapacity());
        }, () -> fail("Store not found"));
    }

    @Test
    void createsWithDefaultCapacity_whenCapacityIsNull() throws JsonProcessingException {
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest();
        storeCreateRequest.setName("Store name");
        storeCreateRequest.setAddress("City, Street, Zipcode");
        storeCreateRequest.setDescription("StoreControllerTest.testCreateEndpoint()");
        // Default capacity is -1.

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeCreateRequest);

        String locationHeader = RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .post("/v1/stores")
            .then()
            .statusCode(201)
            .header("Location", Matchers.startsWith("/v1/stores/"))
            .extract()
            .header("Location");

        long createdId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));
        storeRepository.findByIdAndUserId(createdId, user.getId())
            .ifPresentOrElse(created -> assertEquals(-1, created.getCapacity()), () -> fail("Store not found"));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/stores/{id} endpoint")
    void testGetStoreInfoEndpoint() {
        Store store = storeRepository.save(new Store(user, "Store name", "City, Street, Zipcode",
                "StoreControllerTest.testGetStoreInfoEndpoint()"));

        RestAssured.given(spec)
            .when()
            .get("/v1/stores/{storeId}", store.getId())
            .then()
            .statusCode(200)
            .body("storeId", equalTo(store.getId().intValue()))
            .body("registrant", equalTo(user.getUsername()))
            .body("name", equalTo(store.getName()))
            .body("address", equalTo(store.getAddress()))
            .body("description", equalTo(store.getDescription()));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/stores endpoint")
    void testSearchEndpoint() {
        User user2 = userRepository.save(new User("user2", "password", "hello", "description"));
        storeRepository.save(new Store(user, "Pasta", "address", "Pasta only"));
        storeRepository.save(new Store(user, "Pizza", "address", "Pizza and Pasta"));
        storeRepository.save(new Store(user, "Hamburger", "pasta street", "Hamburger"));
        storeRepository.save(new Store(user, "Korean food", "address", "Kimchi and Bulgogi"));
        storeRepository.save(new Store(user2, "Italian", "address", "Steak and Pasta"));
        storeRepository.save(new Store(user2, "Ramen", "address", "Ramen and Gyoza"));

        RestAssured.given(spec)
            .param("registrant", "username")
            .param("query", "pasta")
            .when()
            .get("/v1/stores")
            .then()
            .statusCode(200)
            .body("count", equalTo(3))
            .body("pageSize", equalTo(20))
            .body("pageNumber", equalTo(0))
            .body("hasNext", equalTo(false))
            .body("results.size()", equalTo(3))
            .body("results[0].name", equalTo("Pasta"))
            .body("results[1].name", equalTo("Pizza"))
            .body("results[2].name", equalTo("Hamburger"));
    }

    @Test
    @DisplayName("[Integration] Testing PUT /v1/stores/{id} endpoint")
    void testUpdateEndpoint() throws JsonProcessingException {
        Store store = storeRepository
            .save(new Store(user, "Store name", "City, Street, Zipcode", "StoreControllerTest.testUpdateEndpoint()"));

        StoreUpdateRequest storeUpdateRequest = new StoreUpdateRequest();
        storeUpdateRequest.setName("New name");
        storeUpdateRequest.setAddress("New address");
        storeUpdateRequest.setDescription("New description");
        storeUpdateRequest.setCapacity(10);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeUpdateRequest);

        RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .put("/v1/stores/{storeId}", store.getId())
            .then()
            .statusCode(200);

        storeRepository.findById(store.getId()).ifPresentOrElse(updatedStore -> {
            assertEquals("New name", updatedStore.getName());
            assertEquals("New address", updatedStore.getAddress());
            assertEquals("New description", updatedStore.getDescription());
            assertEquals(10, updatedStore.getCapacity());
        }, () -> fail("Store not found"));
    }

    @Test
    void preservesCapacity_whenCapacityIsNull() throws JsonProcessingException {
        Store store = storeRepository.save(
                new Store(user, "Store name", "City, Street, Zipcode", "StoreControllerTest.testUpdateEndpoint()", 5));
        int initialCapacity = storeRepository.findById(store.getId()).orElseThrow().getCapacity();

        StoreUpdateRequest storeUpdateRequest = new StoreUpdateRequest();
        storeUpdateRequest.setName("New name");
        storeUpdateRequest.setAddress("New address");
        storeUpdateRequest.setDescription("New description");
        // storeUpdateRequest.capacity is null.

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeUpdateRequest);

        RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(payload)
            .when()
            .put("/v1/stores/{storeId}", store.getId())
            .then()
            .statusCode(200);

        storeRepository.findById(store.getId()).ifPresentOrElse(updatedStore -> {
            assertEquals("New name", updatedStore.getName());
            assertEquals("New address", updatedStore.getAddress());
            assertEquals("New description", updatedStore.getDescription());
            assertEquals(initialCapacity, updatedStore.getCapacity());
        }, () -> fail("Store not found"));
    }

    @Test
    @DisplayName("[Integration] Testing DELETE /v1/stores/{id} endpoint")
    void testDeleteEndpoint() {
        Store store = storeRepository
            .save(new Store(user, "Store name", "City, Street, Zipcode", "StoreControllerTest.testDeleteEndpoint()"));

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured.given(spec)
            .header("Authorization", "Bearer " + signInToken.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/v1/stores/{storeId}", store.getId())
            .then()
            .statusCode(200);

        assertFalse(storeRepository.existsById(store.getId()));
    }

}
