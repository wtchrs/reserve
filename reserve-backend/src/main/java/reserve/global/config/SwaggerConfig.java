package reserve.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reserve.global.exception.ErrorCode;


@Configuration
@Slf4j
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        String jwt = "JWT";
        Components components = new Components();
        // Add JWT authorization
        components.addSecuritySchemes(
                jwt,
                new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Authorization. This will be response with status code 400, 401, 403.")
        );
        // Add ErrorCode schema
        ErrorCode example = ErrorCode.INVALID_ACCESS_TOKEN_FORMAT;
        components.addSchemas(
                "ErrorCode",
                new Schema<>()
                        .type("object")
                        .contentMediaType("application/json")
                        .addProperty(
                                "code",
                                new Schema<>().type("integer").description("Error code").example(example.getCode())
                        )
                        .addProperty(
                                "message",
                                new Schema<>().type("string").description("Message").example(example.getMessage())
                        )
        );
        return new OpenAPI()
                .info(apiInfo())
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("reserve-backend API")
                .description("reserve-backend API Documentation")
                .version("1.0.0");
    }

}
