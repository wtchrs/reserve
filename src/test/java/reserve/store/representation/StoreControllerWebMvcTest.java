package reserve.store.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.dto.response.StoreInfoListResponse;
import reserve.store.dto.response.StoreInfoResponse;
import reserve.store.service.StoreService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
@Import(JwtProvider.class)
class StoreControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @MockBean
    StoreService storeService;

    @Test
    @DisplayName("Testing POST /v1/stores endpoint")
    void testCreateEndpoint() throws Exception {
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest();
        storeCreateRequest.setName("Store name");
        storeCreateRequest.setAddress("City, Street, Zipcode");
        storeCreateRequest.setDescription("Store description");

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(1L));

        Mockito.when(storeService.create(
                Mockito.eq(1L),
                Mockito.argThat(
                        request -> "Store name".equals(request.getName()) &&
                                   "City, Street, Zipcode".equals(request.getAddress()) &&
                                   "Store description".equals(request.getDescription())
                )
        )).thenReturn(10L);

        mockMvc.perform(
                post("/v1/stores")
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storeCreateRequest))
        ).andExpectAll(
                status().isCreated(),
                header().string("Location", "/v1/stores/10"),
                content().string("")
        );
    }

    @Test
    @DisplayName("Testing GET /v1/stores/{id} endpoint")
    void testGetStoreInfoEndpoint() throws Exception {
        Mockito.when(storeService.getStoreInfo(10L)).thenReturn(
                new StoreInfoResponse(10L, "username", "Store name", "City, Street, Zipcode", "Store description")
        );

        mockMvc.perform(
                get("/v1/stores/{id}", 10L)
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.storeId").value(10L),
                jsonPath("$.registrant").value("username"),
                jsonPath("$.name").value("Store name"),
                jsonPath("$.address").value("City, Street, Zipcode"),
                jsonPath("$.description").value("Store description")
        );
    }

    @Test
    @DisplayName("Testing GET /v1/stores endpoint")
    void testSearchEndpoint() throws Exception {
        List<StoreInfoResponse> storeInfoResponses = List.of(
                new StoreInfoResponse(1L, "username", "Pasta", "address", "Pasta only"),
                new StoreInfoResponse(2L, "username", "Pizza", "address", "Pizza and Pasta"),
                new StoreInfoResponse(3L, "username", "Hamburger", "pasta street", "Hamburger")
        );

        Mockito.when(storeService.search(
                Mockito.argThat(
                        request -> "username".equals(request.getRegistrant()) &&
                                   "pasta".equals(request.getQuery())
                ),
                Mockito.any()
        )).thenReturn(StoreInfoListResponse.from(new PageImpl<>(storeInfoResponses, PageRequest.of(0, 20), 3)));

        mockMvc.perform(
                get("/v1/stores")
                        .param("registrant", "username")
                        .param("query", "pasta")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.count").value(3),
                jsonPath("$.pageSize").value(20),
                jsonPath("$.pageNumber").value(0),
                jsonPath("$.hasNext").value(false),
                jsonPath("$.results.length()").value(3),
                jsonPath("$.results[0].name").value("Pasta"),
                jsonPath("$.results[1].name").value("Pizza"),
                jsonPath("$.results[2].name").value("Hamburger")
        );
    }

    @Test
    @DisplayName("Testing PUT /v1/stores/{id} endpoint")
    void testUpdateEndpoint() throws Exception {
        StoreUpdateRequest storeUpdateRequest = new StoreUpdateRequest();
        storeUpdateRequest.setName("New name");
        storeUpdateRequest.setAddress("New address");
        storeUpdateRequest.setDescription("New description");

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(1L));

        mockMvc.perform(
                put("/v1/stores/{id}", 10L)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storeUpdateRequest))
        ).andExpect(status().isOk());

        Mockito.verify(storeService).update(
                Mockito.eq(1L),
                Mockito.eq(10L),
                Mockito.argThat(
                        request -> "New name".equals(request.getName()) &&
                                   "New address".equals(request.getAddress()) &&
                                   "New description".equals(request.getDescription())
                )
        );
    }

    @Test
    @DisplayName("Testing DELETE /v1/stores/{id} endpoint")
    void testDeleteEndpoint() throws Exception {
        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(1L));

        mockMvc.perform(
                delete("/v1/stores/{id}", 10L)
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        Mockito.verify(storeService).delete(1L, 10L);
    }

}
