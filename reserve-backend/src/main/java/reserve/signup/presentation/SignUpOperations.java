package reserve.signup.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reserve.signup.dto.request.SignUpRequest;

@Tag(name = "Sign Up", description = "Sign up API")
public interface SignUpOperations {

    @Operation(
            summary = "Sign up",
            description = "Sign up",
            operationId = "1_signUp"
    )
    @SuppressWarnings("unused")
    ResponseEntity<Void> signUp(SignUpRequest signUpRequest);

}
