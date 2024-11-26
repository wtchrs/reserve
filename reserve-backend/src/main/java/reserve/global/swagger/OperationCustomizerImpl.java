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

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OperationCustomizerImpl implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Map<String, List<ErrorCode>> errorCodeMap = new HashMap<>();
        // Add SecurityRequirement to indicate JWT Authentication
        Streams.of(handlerMethod.getMethod().getParameters())
                .filter(p -> p.getType().equals(AuthInfo.class))
                .filter(p -> p.isAnnotationPresent(Authentication.class))
                .findFirst()
                .ifPresent(p -> addSecurityRequirement(errorCodeMap, operation));
        // Add ApiErrorCodeResponses as ApiResponses to the operation
        ApiErrorCodeResponses annot = handlerMethod.getMethodAnnotation(ApiErrorCodeResponses.class);
        if (annot != null) {
            Streams.of(annot.value()).forEach(a -> addApiErrorCodeResponse(errorCodeMap, a));
        }
        // Apply errorCodeMap to the operation
        errorCodeMap.forEach((key, errorCodes) -> applyToOperation(operation, key, errorCodes));
        return operation;
    }

    private void addSecurityRequirement(Map<String, List<ErrorCode>> errorCodeMap, Operation operation) {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
        operation.addSecurityItem(securityRequirement);
        addApiErrorCodeResponse(errorCodeMap, "400", ErrorCode.INVALID_ACCESS_TOKEN_FORMAT);
        addApiErrorCodeResponse(errorCodeMap, "401", ErrorCode.EXPIRED_ACCESS_TOKEN);
        addApiErrorCodeResponse(errorCodeMap, "403", ErrorCode.SIGN_IN_REQUIRED);
    }

    private void addApiErrorCodeResponse(Map<String, List<ErrorCode>> errorCodeMap, ApiErrorCodeResponse annot) {
        addApiErrorCodeResponse(errorCodeMap, annot.responseCode(), annot.errorCode());
    }

    private void addApiErrorCodeResponse(
            Map<String, List<ErrorCode>> errorCodeMap, String responseCode, ErrorCode errorCode
    ) {
        errorCodeMap
                .computeIfAbsent(responseCode, key -> new LinkedList<>())
                .add(errorCode);
    }

    private void applyToOperation(Operation operation, String responseCode, List<ErrorCode> errorCodes) {
        // Description for all possible ErrorCode
        String itemsStr = errorCodes.stream().map(this::getErrorCodeTableDescription).collect(Collectors.joining("\n"));
        String description = "| Error Code | Message |\n| - | - |\n" + itemsStr;
        // New Schema for the ErrorCode
        Schema<?> schema = new Schema<>().$ref("ErrorCode");
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);
        ApiResponse response = new ApiResponse().content(content).description(description);
        operation.getResponses().addApiResponse(responseCode, response);
    }

    private String getErrorCodeTableDescription(ErrorCode errorCode) {
        return String.format("| %s | %s |", errorCode.getCode(), errorCode.getMessage());
    }

}
