package reserve.store.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class StoreControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    User user;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        user = userRepository.save(new User("username", "password", "nickname", "description"));
    }

    @Test
    @DisplayName("Testing POST /v1/stores endpoint")
    void testCreateEndpoint() throws Exception {
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest();
        storeCreateRequest.setName("Store name");
        storeCreateRequest.setAddress("City, Street, Zipcode");
        storeCreateRequest.setDescription("Store description");

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                post("/v1/stores")
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storeCreateRequest))
        ).andExpectAll(
                status().isCreated(),
                header().string("Location", Matchers.startsWith("/v1/stores/")),
                content().string("")
        );

        assertEquals(1, storeRepository.count());
    }

    @Test
    @DisplayName("Testing GET /v1/stores/{id} endpoint")
    void testGetStoreInfoEndpoint() throws Exception {
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "Store description"
        ));

        mockMvc.perform(
                get("/v1/stores/{id}", store.getId())
        ).andExpectAll(
                status().isOk(),
                content().contentType("application/json"),
                jsonPath("$.storeId").value(store.getId()),
                jsonPath("$.registrant").value("username"),
                jsonPath("$.name").value("Store name"),
                jsonPath("$.address").value("City, Street, Zipcode"),
                jsonPath("$.description").value("Store description")
        );
    }

    @Nested
    class StoreSearchTest {

        @BeforeEach
        @Transactional
        @Commit
        void registerStores() {
            User user2 = userRepository.save(new User("user2", "password", "hello", "description"));
            storeRepository.save(new Store(user, "Pasta", "address", "Pasta only"));
            storeRepository.save(new Store(user, "Pizza", "address", "Pizza and Pasta"));
            storeRepository.save(new Store(user, "Hamburger", "pasta street", "Hamburger"));
            storeRepository.save(new Store(user, "Korean food", "address", "Kimchi and Bulgogi"));
            storeRepository.save(new Store(user2, "Italian", "address", "Steak and Pasta"));
            storeRepository.save(new Store(user2, "Ramen", "address", "Ramen and Gyoza"));
        }

        @AfterEach
        @Transactional
        @Commit
        void tearDown() {
            storeRepository.deleteAll();
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("Testing GET /v1/stores endpoint")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void testSearchEndpoint() throws Exception {
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

    }

    @Test
    @DisplayName("Testing PUT /v1/stores/{id} endpoint")
    void testUpdateEndpoint() throws Exception {
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "Store description"
        ));

        StoreUpdateRequest storeUpdateRequest = new StoreUpdateRequest();
        storeUpdateRequest.setName("New name");
        storeUpdateRequest.setAddress("New address");
        storeUpdateRequest.setDescription("New description");

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                put("/v1/stores/{id}", store.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storeUpdateRequest))
        ).andExpect(status().isOk());

        storeRepository.findById(store.getId()).ifPresentOrElse(
                updatedStore -> {
                    assertEquals("New name", updatedStore.getName());
                    assertEquals("New address", updatedStore.getAddress());
                    assertEquals("New description", updatedStore.getDescription());
                },
                () -> fail("Store not found")
        );
    }

    @Test
    @DisplayName("Testing DELETE /v1/stores/{id} endpoint")
    void testDeleteEndpoint() throws Exception {
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "Store description"
        ));

        SignInToken signInToken = jwtProvider.generateSignInToken(String.valueOf(user.getId()));

        mockMvc.perform(
                delete("/v1/stores/{id}", store.getId())
                        .header("Authorization", "Bearer " + signInToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        assertFalse(storeRepository.existsById(store.getId()));
    }

}
