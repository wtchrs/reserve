package reserve.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        String jwt = "JWT";
        // Add `Authorize` button
        Components components = new Components().addSecuritySchemes(
                jwt,
                new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
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
