package reserve.signup.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reserve.signup.dto.request.SignUpRequest;
import reserve.signup.service.SignUpService;

import java.net.URI;

@RestController
@RequestMapping("/v1/sign-up")
@RequiredArgsConstructor
public class SignUpController implements SignUpOperations {

    private final SignUpService signUpService;

    @Override
    @PostMapping
    public ResponseEntity<Void> signUp(@RequestBody @Validated SignUpRequest signUpRequest) {
        signUpService.signUp(signUpRequest);
        return ResponseEntity.created(URI.create("/v1/users/" + signUpRequest.getUsername())).build();
    }

}
