package reserve.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.signup.infrastructure.Pbkdf2PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder();
    }

}
