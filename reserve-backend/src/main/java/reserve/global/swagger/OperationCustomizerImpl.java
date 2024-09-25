package reserve.global.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.stream.Streams;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;

@Component
@Slf4j
public class OperationCustomizerImpl implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // Add SecurityRequirement to indicate JWT Authentication
        Streams.of(handlerMethod.getMethod().getParameters())
                .filter(p -> p.getType().equals(AuthInfo.class))
                .filter(p -> p.isAnnotationPresent(Authentication.class))
                .findFirst()
                .ifPresent(p -> addSecurityRequirement(operation));

        // Add ApiErrorCodeResponses as ApiResponses to the operation
        ApiErrorCodeResponses annot = handlerMethod.getMethodAnnotation(ApiErrorCodeResponses.class);
        if (annot != null) {
            Streams.of(annot.value()).forEach(a -> addApiErrorCodeResponse(operation, a));
        }

        return operation;
    }

    private void addSecurityRequirement(Operation operation) {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
        operation.addSecurityItem(securityRequirement);
        addApiErrorCodeResponse(operation, "400", ErrorCode.INVALID_ACCESS_TOKEN_FORMAT);
        addApiErrorCodeResponse(operation, "401", ErrorCode.EXPIRED_ACCESS_TOKEN);
        addApiErrorCodeResponse(operation, "403", ErrorCode.SIGN_IN_REQUIRED);
    }

    private void addApiErrorCodeResponse(Operation operation, ApiErrorCodeResponse annot) {
        addApiErrorCodeResponse(operation, annot.responseCode(), annot.errorCode());
    }

    private void addApiErrorCodeResponse(Operation operation, String responseCode, ErrorCode errorCode) {
        // New Schema for the ErrorCode
        Schema<ErrorCode> schema = new Schema<>();
        schema.setType("object");
        schema.addProperty("code", new Schema<>().type("integer").example(errorCode.getCode()));
        schema.addProperty("message", new Schema<>().type("string").example(errorCode.getMessage()));
        // Put the Schema into the ApiResponse
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);
        ApiResponse response = new ApiResponse().content(content).description(errorCode.getMessage());
        operation.getResponses().addApiResponse(getErrorResponseCode(responseCode, errorCode), response);
    }

    private String getErrorResponseCode(String responseCode, ErrorCode errorCode) {
        return responseCode + " (ErrorCode: " + errorCode.getCode() + ")";
    }

}
