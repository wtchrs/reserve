package reserve.signup.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class Pbkdf2PasswordEncoderTest {

    final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Test
    @DisplayName("Testing password encoding")
    void testPasswordEncoding() {
        String test = "1234";
        String encode = passwordEncoder.encode(test);
        assertThat(encode).isNotNull();
    }

    @Test
    @DisplayName("Testing password match")
    void testPasswordMatch() {
        String test = "1324";
        String encode = passwordEncoder.encode(test);
        assertThat(passwordEncoder.matches(test, encode)).isTrue();
    }

}