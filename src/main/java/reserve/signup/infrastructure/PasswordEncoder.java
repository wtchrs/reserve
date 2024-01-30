package reserve.signup.infrastructure;

public interface PasswordEncoder {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, CharSequence encodedPassword);

}
