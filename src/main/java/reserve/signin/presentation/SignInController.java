package reserve.signin.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.dto.response.SignInResponse;
import reserve.signin.service.SignInService;

@RestController
@RequestMapping("/v1")
public class SignInController {

    private final int refreshTokenExpire;

    private final SignInService signInService;

    public SignInController(
            @Value("${application.security.jwt.refreshTokenExpire}") int refreshTokenExpire,
            SignInService signInService
    ) {
        this.refreshTokenExpire = refreshTokenExpire;
        this.signInService = signInService;
    }

    @PostMapping("/sign-in")
    public SignInResponse signIn(@RequestBody @Validated SignInRequest signInRequest, HttpServletResponse response) {
        SignInToken signInToken = signInService.signIn(signInRequest);
        response.addCookie(createRefreshCookie(signInToken));
        return new SignInResponse(signInToken.getAccessToken());
    }

    @PostMapping("/token-refresh")
    public SignInResponse refreshAccessToken(
            @CookieValue("refresh") Cookie refreshCookie,
            HttpServletResponse response
    ) {
        String refreshTokenValue = refreshCookie.getValue();
        SignInToken signInToken = signInService.refreshAccessToken(refreshTokenValue);
        response.addCookie(createRefreshCookie(signInToken));
        return new SignInResponse(signInToken.getAccessToken());
    }

    private Cookie createRefreshCookie(SignInToken signInToken) {
        Cookie newRefreshCookie = new Cookie("refresh", signInToken.getRefreshToken());
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setMaxAge(refreshTokenExpire);
        newRefreshCookie.setPath("/");
        newRefreshCookie.setSecure(true);
        return newRefreshCookie;
    }

    @PostMapping("/sign-out")
    public void signOut(@CookieValue("refresh") Cookie refreshCookie, HttpServletResponse response) {
        signInService.signOut(refreshCookie.getValue());
        // delete cookie
        refreshCookie.setMaxAge(0);
        refreshCookie.setValue("");
        response.addCookie(refreshCookie);
    }

}
