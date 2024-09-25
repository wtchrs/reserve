package reserve.global.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import reserve.global.exception.*;
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
        HttpServletRequest nativeRequest = ((HttpServletRequest) ((ServletWebRequest) request).getNativeRequest());
        log.warn("Request URL: {}, Error message: {}", nativeRequest.getRequestURL(), ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage(),
                paramErrors
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorCode handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return ErrorCode.INVALID_REQUEST;
    }

    @ExceptionHandler(WrongCredentialException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorCode handleWrongCredential(WrongCredentialException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorCode handleInvalidAuthorization(InvalidAuthorizationException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorCode handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(AccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorCode handleAccessToken(AccessTokenException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(RefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorCode handleRefreshToken(RefreshTokenException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(ReservationStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorCode handleReservationStatus(ReservationStatusException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorCode handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(InvalidAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorCode handleAccessDenied(InvalidAccessException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(UsernameDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorCode handleUsernameDuplicate(UsernameDuplicateException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorCode handleException(Exception e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private static ErrorCode handleErrorCodeException(ErrorCodeException e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return e.getErrorCode();
    }

}
