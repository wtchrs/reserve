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
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return new ErrorResponse(ErrorCode.INVALID_REQUEST.getCode(), ErrorCode.INVALID_REQUEST.getMessage());
    }

    @ExceptionHandler(WrongCredentialException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleWrongCredential(WrongCredentialException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAuthorization(InvalidAuthorizationException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(AccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAccessToken(AccessTokenException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(RefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleRefreshToken(RefreshTokenException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(ReservationStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleReservationStatus(ReservationStatusException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(InvalidAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(InvalidAccessException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(UsernameDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameDuplicate(UsernameDuplicateException e, HttpServletRequest request) {
        return handleErrorCodeException(e, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }

    private static ErrorResponse handleErrorCodeException(ErrorCodeException e, HttpServletRequest request) {
        log.warn("Request URL: {}, Error message: {}", request.getRequestURL(), e.getMessage(), e);
        return new ErrorResponse(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
    }

}
