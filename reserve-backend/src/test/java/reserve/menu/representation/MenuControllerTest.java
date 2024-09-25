package reserve.menu.representation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import reserve.global.BaseRestAssuredTest;
import reserve.global.TestUtils;
import reserve.menu.domain.Menu;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.infrastructure.MenuRepository;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuControllerTest extends BaseRestAssuredTest {

    @Autowired
    DataSource dataSource;

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

    User user;
    Store store;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("username", "password", "nickname", "description"));
        store = storeRepository.save(new Store(user, "Italian Restaurant", "address", "Pasta and Pizza"));
    }

    @AfterEach
    void tearDown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM menus");
        jdbcTemplate.update("DELETE FROM stores");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/stores/{storeId}/menus endpoint")
    void testCreateMenuEndpoint() throws JsonProcessingException {
        MenuCreateRequest menuCreateRequest = new MenuCreateRequest();
        menuCreateRequest.setName("Aglio e Olio");
        menuCreateRequest.setPrice(10000);
        menuCreateRequest.setDescription("Spaghetti with garlic and olive oil");

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(menuCreateRequest);

        Response response = RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .when()
                .post("/v1/stores/{storeId}/menus", store.getId());

        response.then().statusCode(201).header("Location", startsWith("/v1/menus/"));

        String location = response.getHeader("Location");
        long menuId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        menuRepository.findById(menuId).ifPresentOrElse(
                menu -> {
                    assertEquals(menuCreateRequest.getName(), menu.getName());
                    assertEquals(menuCreateRequest.getPrice(), menu.getPrice());
                    assertEquals(menuCreateRequest.getDescription(), menu.getDescription());
                },
                () -> fail("Menu not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/menus/{menuId} endpoint")
    void testGetMenuInfoEndpoint() {
        Menu menu = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        RestAssured
                .given(spec)
                .relaxedHTTPSValidation()
                .when().get("/v1/menus/{menuId}", menu.getId())
                .then()
                .statusCode(200)
                .body("menuId", equalTo(menu.getId().intValue()))
                .body("storeId", equalTo(store.getId().intValue()))
                .body("name", equalTo(menu.getName()))
                .body("price", equalTo(menu.getPrice()))
                .body("description", equalTo(menu.getDescription()));
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/stores/{storeId}/menus endpoint")
    void testGetStoreMenusEndpoint() {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));
        Menu menu2 = menuRepository.save(new Menu(store, "Carbonara", 12000, "Spaghetti with bacon, eggs, and cheese"));
        Menu menu3 = menuRepository.save(new Menu(store, "Bolognese", 12000, "Spaghetti with meat sauce"));

        RestAssured
                .given(spec)
                .relaxedHTTPSValidation()
                .when().get("/v1/stores/{storeId}/menus", store.getId())
                .then()
                .statusCode(200)
                .body("count", equalTo(3))
                .body(
                        "results.menuId",
                        contains(menu1.getId().intValue(), menu2.getId().intValue(), menu3.getId().intValue())
                )
                .body(
                        "results.storeId",
                        contains(store.getId().intValue(), store.getId().intValue(), store.getId().intValue())
                )
                .body("results.name", contains(menu1.getName(), menu2.getName(), menu3.getName()))
                .body("results.price", contains(menu1.getPrice(), menu2.getPrice(), menu3.getPrice()))
                .body(
                        "results.description",
                        contains(menu1.getDescription(), menu2.getDescription(), menu3.getDescription())
                );
    }

    @Test
    @DisplayName("[Integration] Testing PUT /v1/menus/{menuId} endpoint")
    void testUpdateMenuEndpoint() throws JsonProcessingException {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        MenuUpdateRequest menuUpdateRequest = new MenuUpdateRequest();
        menuUpdateRequest.setName("Spaghetti Aglio e Olio");
        menuUpdateRequest.setPrice(12000);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(menuUpdateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .when().put("/v1/menus/{menuId}", menu1.getId())
                .then().statusCode(200);

        menuRepository.findById(menu1.getId()).ifPresentOrElse(
                menu -> {
                    assertEquals(menuUpdateRequest.getName(), menu.getName());
                    assertEquals(menuUpdateRequest.getPrice(), menu.getPrice());
                    assertEquals("Spaghetti with garlic and olive oil", menu.getDescription());
                },
                () -> fail("Menu not found")
        );
    }

    @Test
    @DisplayName("[Integration] Testing DELETE /v1/menus/{menuId} endpoint")
    void testDeleteMenuEndpoint() {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .relaxedHTTPSValidation()
                .when().delete("/v1/menus/{menuId}", menu1.getId())
                .then().statusCode(200);

        assertFalse(menuRepository.existsById(menu1.getId()));
    }

}
