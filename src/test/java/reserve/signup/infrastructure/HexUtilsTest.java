package reserve.signup.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HexUtilsTest {

    @Test
    @DisplayName("Testing conversion of bytes to hexadecimal")
    void testBytesToHexConversion() {
        byte[] testBytes = "Hello!".getBytes();
        String expectedHex = "48656C6C6F21";
        String hex = HexUtils.bytesToHex(testBytes);
        Assertions.assertThat(hex).isEqualTo(expectedHex);
    }

    @Test
    @DisplayName("Testing conversion of hexadecimal to bytes")
    void testHexToBytesConversion() {
        String test = "48656C6C6F21";
        byte[] expectedBytes = "Hello!".getBytes();
        byte[] bytes = HexUtils.hexToBytes(test);
        Assertions.assertThat(bytes).isEqualTo(expectedBytes);
    }

}
