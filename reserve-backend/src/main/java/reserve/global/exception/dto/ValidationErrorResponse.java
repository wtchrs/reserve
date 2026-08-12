package reserve.global.exception.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ValidationErrorResponse {

    private final int errorCode;

    private final String message;

    private final List<ParameterError> invalidParams;

}
