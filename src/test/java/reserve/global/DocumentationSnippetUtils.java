package reserve.global;

import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public abstract class DocumentationSnippetUtils {

    public static RequestHeadersSnippet bearerTokenAuthorizationSnippet() {
        return requestHeaders(headerWithName("Authorization").description("The access token in bearer scheme"));
    }

    public static ResponseFieldsSnippet errorResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("errorCode").description("The error code"),
                fieldWithPath("message").description("The error message")
        );
    }

}
