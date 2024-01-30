package reserve.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.dto.ErrorResponse;
import reserve.global.exception.UsernameDuplicateException;
import reserve.global.exception.dto.ParameterError;
import reserve.global.exception.dto.ValidationErrorResponse;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ParameterError> paramErrors = fieldErrors.stream().map(ParameterError::from).toList();
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(new ValidationErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage(),
                paramErrors
        ));
    }

    @ExceptionHandler(UsernameDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleUsernameDuplicateException(UsernameDuplicateException e) {
        log.warn(e.getMessage(), e);
        ErrorResponse body = new ErrorResponse(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

}
