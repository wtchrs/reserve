package reserve.signup.infrastructure;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Pbkdf2PasswordEncoder implements PasswordEncoder {

    public static final int DEFAULT_ITERATIONS = 65536;
    public static final int DEFAULT_HASH_LENGTH = 256;
    public static final int DEFAULT_SALT_LENGTH = 20;

    private final int iterations, hashLength, saltLength;

    public Pbkdf2PasswordEncoder(int iterations, int hashLength, int saltLength) {
        this.iterations = iterations;
        this.hashLength = hashLength;
        this.saltLength = saltLength;
    }

    public Pbkdf2PasswordEncoder() {
        this(DEFAULT_ITERATIONS, DEFAULT_HASH_LENGTH, DEFAULT_SALT_LENGTH);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = generateRandomSalt();
        byte[] hash = generateHash(rawPassword, salt);
        return HexUtils.bytesToHex(salt, hash);
    }

    private byte[] generateRandomSalt() {
        byte[] salt = new byte[saltLength];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    @Override
    public boolean matches(CharSequence rawPassword, CharSequence encodedPassword) {
        byte[] encodedPasswordBytes = HexUtils.hexToBytes(encodedPassword);
        byte[] salt = extractSalt(encodedPasswordBytes);
        byte[] hash = extractHash(encodedPasswordBytes);
        return Arrays.equals(hash, generateHash(rawPassword, salt));
    }

    private byte[] extractSalt(byte[] encodedPasswordBytes) {
        byte[] salt = new byte[saltLength];
        System.arraycopy(encodedPasswordBytes, 0, salt, 0, saltLength);
        return salt;
    }

    private byte[] extractHash(byte[] encodedPasswordBytes) {
        byte[] hash = new byte[encodedPasswordBytes.length - saltLength];
        System.arraycopy(encodedPasswordBytes, saltLength, hash, 0, hash.length);
        return hash;
    }

    private byte[] generateHash(CharSequence rawPassword, byte[] salt) {
        KeySpec keySpec = new PBEKeySpec(rawPassword.toString().toCharArray(), salt, iterations, hashLength);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey secretKey = factory.generateSecret(keySpec);
            return secretKey.getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}