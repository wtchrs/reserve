package reserve.store.representation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.QueryParametersSnippet;
import reserve.global.BaseRestAssuredTest;
import reserve.global.TestUtils;
import reserve.signin.dto.SignInToken;
import reserve.signin.infrastructure.JwtProvider;
import reserve.store.domain.Store;
import reserve.store.dto.request.StoreCreateRequest;
import reserve.store.dto.request.StoreUpdateRequest;
import reserve.store.infrastructure.StoreRepository;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;

import javax.sql.DataSource;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static reserve.global.DocumentationSnippetUtils.bearerTokenAuthorizationSnippet;

class StoreControllerTest extends BaseRestAssuredTest {

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

    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(
                "username",
                "password",
                "nickname",
                "StoreControllerTest.setUp()"
        ));
    }

    @AfterEach
    void tearDown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM stores");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("[Integration] Testing POST /v1/stores endpoint")
    void testCreateEndpoint() throws JsonProcessingException {
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest();
        storeCreateRequest.setName("Store name");
        storeCreateRequest.setAddress("City, Street, Zipcode");
        storeCreateRequest.setDescription("StoreControllerTest.testCreateEndpoint()");

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeCreateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        storeCreateRequestFieldsSnippet(),
                        responseHeaders(headerWithName("Location").description("The url of the created store"))
                ))
                .when().post("/v1/stores")
                .then()
                .statusCode(201)
                .header("Location", Matchers.startsWith("/v1/stores/"));

        assertEquals(1, storeRepository.count());
    }

    @Test
    @DisplayName("[Integration] Testing GET /v1/stores/{id} endpoint")
    void testGetStoreInfoEndpoint() {
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "StoreControllerTest.testGetStoreInfoEndpoint()"
        ));

        RestAssured
                .given(spec)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        storeIdToRetrievePathParametersSnippet(),
                        storeInfoResponseFieldsSnippet()
                ))
                .when().get("/v1/stores/{storeId}", store.getId())
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

        RestAssured
                .given(spec).param("registrant", "username").param("query", "pasta")
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        storeSearchResponseQueryParametersSnippet(),
                        storeInfoListResponseFieldsSnippet()
                ))
                .when().get("/v1/stores")
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
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "StoreControllerTest.testUpdateEndpoint()"
        ));

        StoreUpdateRequest storeUpdateRequest = new StoreUpdateRequest();
        storeUpdateRequest.setName("New name");
        storeUpdateRequest.setAddress("New address");
        storeUpdateRequest.setDescription("New description");

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        String payload = objectMapper.writeValueAsString(storeUpdateRequest);

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        storeIdToUpdatePathParametersSnippet(),
                        storeUpdateRequestFieldsSnippet()
                ))
                .when().put("/v1/stores/{storeId}", store.getId())
                .then()
                .statusCode(200);

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
    @DisplayName("[Integration] Testing DELETE /v1/stores/{id} endpoint")
    void testDeleteEndpoint() {
        Store store = storeRepository.save(new Store(
                user,
                "Store name",
                "City, Street, Zipcode",
                "StoreControllerTest.testDeleteEndpoint()"
        ));

        SignInToken signInToken = jwtProvider.generateSignInToken(TestUtils.getTokenDetails(user));

        RestAssured
                .given(spec)
                .header("Authorization", "Bearer " + signInToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .relaxedHTTPSValidation()
                .filter(document(
                        DEFAULT_RESTDOC_PATH,
                        bearerTokenAuthorizationSnippet(),
                        storeIdToDeletePathParametersSnippet()
                ))
                .when().delete("/v1/stores/{storeId}", store.getId())
                .then()
                .statusCode(200);

        assertFalse(storeRepository.existsById(store.getId()));
    }

    private static PathParametersSnippet storeIdToRetrievePathParametersSnippet() {
        return pathParameters(
                parameterWithName("storeId").description("The id of the store to retrieve the store info")
        );
    }

    private static PathParametersSnippet storeIdToUpdatePathParametersSnippet() {
        return pathParameters(parameterWithName("storeId").description("The ID of the store to update"));
    }

    private static PathParametersSnippet storeIdToDeletePathParametersSnippet() {
        return pathParameters(parameterWithName("storeId").description("The ID of the store to delete"));
    }

    /**
     * @return The request fields snippet
     * @see StoreCreateRequest
     */
    private static RequestFieldsSnippet storeCreateRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("name").description("Store name"),
                fieldWithPath("address").description("Store address"),
                fieldWithPath("description").description("Store description")
        );
    }

    /**
     * @return The request fields snippet
     * @see StoreUpdateRequest
     */
    private static RequestFieldsSnippet storeUpdateRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("name").description("Store name"),
                fieldWithPath("address").description("Store address"),
                fieldWithPath("description").description("Store description")
        );
    }

    /**
     * @return The query parameters snippet
     * @see reserve.store.dto.request.StoreSearchRequest
     */
    private static QueryParametersSnippet storeSearchResponseQueryParametersSnippet() {
        return queryParameters(
                parameterWithName("registrant").description("The username of the registrant"),
                parameterWithName("query").description("The query to search the stores")
        );
    }

    /**
     * @return The request fields snippet
     * @see reserve.store.dto.response.StoreInfoResponse
     */
    private static ResponseFieldsSnippet storeInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("storeId").description("The id of the store"),
                fieldWithPath("registrant").description("The username of the registrant"),
                fieldWithPath("name").description("The name of the store"),
                fieldWithPath("address").description("The address of the store"),
                fieldWithPath("description").description("The description of the store")
        );
    }

    /**
     * @return The response fields snippet
     * @see reserve.store.dto.response.StoreInfoListResponse
     */
    private static ResponseFieldsSnippet storeInfoListResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("count").description("The total count of the search results"),
                fieldWithPath("pageSize").description("The page size of the search results"),
                fieldWithPath("pageNumber").description("The page number of the search results"),
                fieldWithPath("hasNext").description("The existence of the next page"),
                fieldWithPath("results").description("The search results"),
                fieldWithPath("results[].storeId").description("The id of the store"),
                fieldWithPath("results[].registrant").description("The username of the registrant"),
                fieldWithPath("results[].name").description("The name of the store"),
                fieldWithPath("results[].address").description("The address of the store"),
                fieldWithPath("results[].description").description("The description of the store")
        );
    }

}
