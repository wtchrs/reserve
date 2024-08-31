package reserve.menu.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import reserve.global.TestUtils;
import reserve.menu.dto.request.MenuCreateRequest;
import reserve.menu.dto.request.MenuUpdateRequest;
import reserve.menu.dto.response.MenuInfoListResponse;
import reserve.menu.dto.response.MenuInfoResponse;
import reserve.menu.service.MenuService;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@Import(JwtProvider.class)
class MenuControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    MenuService menuService;

    @Test
    @DisplayName("Testing POST /v1/stores/{storeId}/menus endpoint")
    void testCreateMenuEndpoint() throws Exception {
        Long userId = 1L;
        Long storeId = 10L;

        MenuCreateRequest menuCreateRequest = new MenuCreateRequest();
        menuCreateRequest.setName("Aglio e Olio");
        menuCreateRequest.setPrice(10000);
        menuCreateRequest.setDescription("Spaghetti with garlic and olive oil");

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        Mockito.when(menuService.create(
                Mockito.eq(userId),
                Mockito.eq(storeId),
                Mockito.argThat(
                        arg -> arg.getName().equals(menuCreateRequest.getName()) &&
                               arg.getPrice() == menuCreateRequest.getPrice() &&
                               arg.getDescription().equals(menuCreateRequest.getDescription())
                )
        )).thenReturn(100L);

        mockMvc.perform(
                post("/v1/stores/{storeId}/menus", storeId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(menuCreateRequest))
        ).andExpectAll(
                status().isCreated(),
                header().string("Location", "/v1/menus/100")
        );
    }

    @Test
    @DisplayName("Testing GET /v1/menus/{menuId} endpoint")
    void testGetMenuInfoEndpoint() throws Exception {
        Long storeId = 10L;
        Long menuId = 100L;

        MenuInfoResponse response = new MenuInfoResponse(
                menuId,
                storeId,
                "Aglio e Olio",
                10000,
                "Spaghetti with garlic and olive oil"
        );

        Mockito.when(menuService.getMenuInfo(menuId)).thenReturn(response);

        mockMvc.perform(
                get("/v1/menus/{menuId}", menuId)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.menuId").value(response.getMenuId()),
                jsonPath("$.storeId").value(response.getStoreId()),
                jsonPath("$.name").value(response.getName()),
                jsonPath("$.price").value(response.getPrice()),
                jsonPath("$.description").value(response.getDescription())
        );
    }

    @Test
    @DisplayName("Testing GET /v1/stores/{storeId}/menus endpoint")
    void testGetStoreMenusEndpoint() throws Exception {
        Long storeId = 10L;
        MenuInfoResponse menu1 =
                new MenuInfoResponse(100L, storeId, "Aglio e Olio", 10000, "Spaghetti with garlic and olive oil");
        MenuInfoResponse menu2 =
                new MenuInfoResponse(101L, storeId, "Carbonara", 12000, "Spaghetti with bacon, eggs, and cheese");
        MenuInfoResponse menu3 = new MenuInfoResponse(102L, storeId, "Bolognese", 12000, "Spaghetti with meat sauce");

        Mockito.when(menuService.getStoreMenus(storeId))
                .thenReturn(MenuInfoListResponse.from(List.of(menu1, menu2, menu3)));

        mockMvc.perform(
                get("/v1/stores/{storeId}/menus", storeId)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.count").value(3),
                jsonPath("$.results[*].menuId").value(contains(
                        menu1.getMenuId().intValue(),
                        menu2.getMenuId().intValue(),
                        menu3.getMenuId().intValue()
                )),
                jsonPath("$.results[*].storeId").value(contains(
                        menu1.getStoreId().intValue(),
                        menu2.getStoreId().intValue(),
                        menu3.getStoreId().intValue()
                )),
                jsonPath("$.results[*].name")
                        .value(contains(menu1.getName(), menu2.getName(), menu3.getName())),
                jsonPath("$.results[*].price")
                        .value(contains(menu1.getPrice(), menu2.getPrice(), menu3.getPrice())),
                jsonPath("$.results[*].description")
                        .value(contains(menu1.getDescription(), menu2.getDescription(), menu3.getDescription()))
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/menus/{menuId} endpoint")
    void testUpdateMenuEndpoint() throws Exception {
        Long userId = 1L;
        Long menuId = 100L;

        MenuUpdateRequest menuUpdateRequest = new MenuUpdateRequest();
        menuUpdateRequest.setName("Spaghetti Aglio e Olio");
        menuUpdateRequest.setPrice(12000);

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(put("/v1/menus/{menuId}", menuId)
                                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(menuUpdateRequest))
        ).andExpect(status().isOk());

        Mockito.verify(menuService).update(
                Mockito.eq(userId),
                Mockito.eq(menuId),
                Mockito.argThat(
                        arg -> arg.getName().equals(menuUpdateRequest.getName()) &&
                               arg.getPrice().equals(menuUpdateRequest.getPrice())
                )
        );
    }

    @Test
    @DisplayName("Testing DELETE /v1/menus/{menuId} endpoint")
    void testDeleteMenuEndpoint() throws Exception {
        Long userId = 1L;
        Long menuId = 100L;

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(userId));

        mockMvc.perform(
                delete("/v1/menus/{menuId}", menuId)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
        ).andExpect(status().isOk());

        Mockito.verify(menuService).delete(userId, menuId);
    }

}
