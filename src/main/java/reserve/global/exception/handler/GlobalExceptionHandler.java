package reserve.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import reserve.global.exception.*;
import reserve.global.exception.dto.ErrorResponse;
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
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage(),
                paramErrors
        ));
    }

    @ExceptionHandler(WrongCredentialException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleWrongCredential(WrongCredentialException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAuthorization(InvalidAuthorizationException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthentication(AuthenticationException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(AccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAccessToken(AccessTokenException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(RefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleRefreshToken(RefreshTokenException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(UsernameDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameDuplicate(UsernameDuplicateException e) {
        return handleErrorCodeException(e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }

    private static ErrorResponse handleErrorCodeException(ErrorCodeException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
    }

}
