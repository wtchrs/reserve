package reserve.auth.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import reserve.auth.domain.AuthType;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authentication {

    boolean required() default true;

    AuthType type() default AuthType.USER;

}
