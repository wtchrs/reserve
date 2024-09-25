package reserve.signup.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.signup.dto.request.SignUpRequest;

@Tag(name = "Sign Up", description = "Sign up API")
public interface SignUpOperations {

    @Operation(
            summary = "Sign up",
            description = "Sign up",
            operationId = "1_signUp"
    )
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully signed up"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "409", errorCode = ErrorCode.USERNAME_DUPLICATE))
    @SuppressWarnings("unused")
    ResponseEntity<Void> signUp(SignUpRequest signUpRequest);

}
