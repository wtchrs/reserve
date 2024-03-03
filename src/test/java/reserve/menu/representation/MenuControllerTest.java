package reserve.menu.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class MenuControllerTest {

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
    MenuRepository menuRepository;

    User user;
    Store store;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        user = userRepository.save(new User("username", "password", "nickname", "description"));
        store = storeRepository.save(new Store(user, "Italian Restaurant", 1000, "address", "Pasta and Pizza"));
    }

    @Test
    @DisplayName("Testing POST /v1/stores/{storeId}/menus endpoint")
    void testCreateMenuEndpoint() throws Exception {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));
        MenuCreateRequest menuCreateRequest = new MenuCreateRequest();
        menuCreateRequest.setName("Aglio e Olio");
        menuCreateRequest.setPrice(10000);
        menuCreateRequest.setDescription("Spaghetti with garlic and olive oil");

        mockMvc.perform(
                post("/v1/stores/{storeId}/menus", store.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(menuCreateRequest))
        ).andExpect(
                status().isCreated()
        ).andDo(mvcResult -> {
            String location = mvcResult.getResponse().getHeader("Location");
            assertNotNull(location);
            assertTrue(location.startsWith("/v1/menus/"));

            long menuId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
            menuRepository.findById(menuId).ifPresentOrElse(
                    menu -> {
                        assertEquals(menuCreateRequest.getName(), menu.getName());
                        assertEquals(menuCreateRequest.getPrice(), menu.getPrice());
                        assertEquals(menuCreateRequest.getDescription(), menu.getDescription());
                    },
                    () -> fail("Menu not found")
            );
        });
    }

    @Test
    @DisplayName("Testing GET /v1/menus/{menuId} endpoint")
    void testGetMenuInfoEndpoint() throws Exception {
        Menu menu = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        mockMvc.perform(
                get("/v1/menus/{menuId}", menu.getId())
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.menuId").value(menu.getId()),
                jsonPath("$.storeId").value(store.getId()),
                jsonPath("$.name").value(menu.getName()),
                jsonPath("$.price").value(menu.getPrice()),
                jsonPath("$.description").value(menu.getDescription())
        );
    }

    @Test
    @DisplayName("Testing GET /v1/stores/{storeId}/menus endpoint")
    void testGetStoreMenusEndpoint() throws Exception {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));
        Menu menu2 = menuRepository.save(new Menu(store, "Carbonara", 12000, "Spaghetti with bacon, eggs, and cheese"));
        Menu menu3 = menuRepository.save(new Menu(store, "Bolognese", 12000, "Spaghetti with meat sauce"));

        mockMvc.perform(
                get("/v1/stores/{storeId}/menus", store.getId())
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.count").value(3)
        ).andDo(mvcResult -> {
            String content = mvcResult.getResponse().getContentAsString();
            Assertions.assertThat(JsonPath.parse(content).read("$.results[*].menuId", Long[].class))
                    .containsExactly(menu1.getId(), menu2.getId(), menu3.getId());
        });
    }

    @Test
    @DisplayName("Testing PUT /v1/menus/{menuId} endpoint")
    void testUpdateMenuEndpoint() throws Exception {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        MenuUpdateRequest menuUpdateRequest = new MenuUpdateRequest();
        menuUpdateRequest.setName("Spaghetti Aglio e Olio");
        menuUpdateRequest.setPrice(12000);

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(put("/v1/menus/{menuId}", menu1.getId())
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(menuUpdateRequest))
        ).andExpect(status().isOk());

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
    @DisplayName("Testing DELETE /v1/menus/{menuId} endpoint")
    void testDeleteMenuEndpoint() throws Exception {
        Menu menu1 = menuRepository.save(new Menu(store, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil"));

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                delete("/v1/menus/{menuId}", menu1.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        assertFalse(menuRepository.existsById(menu1.getId()));
    }

}
