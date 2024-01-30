package reserve.signup.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class Pbkdf2PasswordEncoderTest {

    final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Test
    void encode() {
        String test = "1234";
        String encode = passwordEncoder.encode(test);
        assertThat(encode).isNotNull();
    }

    @Test
    void matches() {
        String test = "1324";
        String encode = passwordEncoder.encode(test);
        assertThat(passwordEncoder.matches(test, encode)).isTrue();
    }

}