package reserve.global.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.apache.commons.lang3.stream.Streams;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;

@Component
public class OperationCustomizerImpl implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Streams.of(handlerMethod.getMethod().getParameters())
                .filter(p -> p.getType().equals(AuthInfo.class))
                .filter(p -> p.isAnnotationPresent(Authentication.class))
                .findFirst()
                .ifPresent(p -> {
                    // Add SecurityRequirement to indicate JWT Authentication
                    SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
                    operation.addSecurityItem(securityRequirement);
                });
        return operation;
    }

}
