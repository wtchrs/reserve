package reserve.signup.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HexUtilsTest {

    @Test
    void bytesToHex() {
        byte[] testBytes = "Hello!".getBytes();
        String expectedHex = "48656C6C6F21";
        String hex = HexUtils.bytesToHex(testBytes);
        Assertions.assertThat(hex).isEqualTo(expectedHex);
    }

    @Test
    void hexToBytes() {
        String test = "48656C6C6F21";
        byte[] expectedBytes = "Hello!".getBytes();
        byte[] bytes = HexUtils.hexToBytes(test);
        Assertions.assertThat(bytes).isEqualTo(expectedBytes);
    }

}