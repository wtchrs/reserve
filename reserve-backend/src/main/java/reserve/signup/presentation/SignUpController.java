package reserve.signup.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.signup.dto.request.SignUpRequest;
import reserve.signup.service.SignUpService;

@RestController
@RequestMapping("/v1/sign-up")
@RequiredArgsConstructor
@Tag(name = "Sign Up", description = "Sign up API")
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping
    @Operation(summary = "Sign up", description = "Sign up", operationId = "1_signUp")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully signed up"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "409", errorCode = ErrorCode.USERNAME_DUPLICATE))
    public ResponseEntity<Void> signUp(@RequestBody @Validated SignUpRequest signUpRequest) {
        signUpService.signUp(signUpRequest);
        return ResponseEntity.created(URI.create("/v1/users/" + signUpRequest.getUsername())).build();
    }

}
