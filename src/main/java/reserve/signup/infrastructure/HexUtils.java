package reserve.signup.infrastructure;

public abstract class HexUtils {

    private static final char[] DIGIT = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] ...bytes) {
        int totalLength = 0;
        for (byte[] arr : bytes) {
            totalLength += arr.length;
        }
        char[] result = new char[totalLength * 2];
        int pos = 0;
        for (byte[] arr : bytes) {
            for (byte b : arr) {
                result[pos++] = DIGIT[(b & 0xFF) >> 4];
                result[pos++] = DIGIT[b & 0x0F];
            }
        }
        return new String(result);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        int pos = 0;
        for (byte b : bytes) {
            result[pos++] = DIGIT[(b & 0xFF) >> 4];
            result[pos++] = DIGIT[b & 0x0F];
        }
        return new String(result);
    }

    public static byte[] hexToBytes(CharSequence s) {
        int sLen = s.length();
        if (sLen % 2 != 0) {
            throw new IllegalArgumentException("Hex-encoded string must be even length.");
        }
        byte[] result = new byte[sLen / 2];
        for (int i = 0, j = 0; i < sLen; i += 2, j++) {
            int msb = Character.digit(s.charAt(i), 16);
            int lsb = Character.digit(s.charAt(i + 1), 16);
            if (msb < 0 || lsb < 0) {
                throw new IllegalArgumentException("The given string is not a hex-encoded string.");
            }
            result[j] = (byte) (msb << 4 | lsb);
        }
        return result;
    }

}
