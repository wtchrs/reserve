package reserve.global.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

@RequiredArgsConstructor
@Getter
public class ParameterError {

    private final String name;
    private final String reason;

    public static ParameterError from(FieldError fieldError) {
        return new ParameterError(fieldError.getField(), fieldError.getDefaultMessage());
    }

}
