package reserve.global.swagger.annotation;

import reserve.global.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeResponse {

    String responseCode();

    ErrorCode errorCode();

    String description() default "";

}
