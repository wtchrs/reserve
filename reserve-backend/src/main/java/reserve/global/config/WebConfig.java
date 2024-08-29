package reserve.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reserve.auth.infrastructure.AuthInfoArgumentResolver;

import java.util.List;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final AuthInfoArgumentResolver authInfoArgumentResolver;

    private final List<String> allowedOrigins;

    public WebConfig(
            AuthInfoArgumentResolver authInfoArgumentResolver,
            @Value("${application.cors.allowedOrigins}") List<String> allowedOrigins
    ) {
        this.authInfoArgumentResolver = authInfoArgumentResolver;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authInfoArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration corsRegistration = registry.addMapping("/**");
        for (String allowedOrigin : allowedOrigins) {
            log.info("CORS allowed origin: {}", allowedOrigin);
            corsRegistration
                    .allowedOrigins(allowedOrigin)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowCredentials(true)
                    .allowedHeaders(CorsConfiguration.ALL)
                    .exposedHeaders("Authorization", "Set-Cookie");
        }
    }
}
