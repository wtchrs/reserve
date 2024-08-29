package reserve.global.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ValidationErrorResponse {

    private final int errorCode;
    private final String message;
    private final List<ParameterError> invalidParams;

}
