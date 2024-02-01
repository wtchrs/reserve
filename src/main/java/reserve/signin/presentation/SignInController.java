package reserve.signin.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
        Cookie refreshCookie = new Cookie("refresh", signInToken.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(refreshTokenExpire);
        refreshCookie.setPath("/");
        refreshCookie.setSecure(true);
        response.addCookie(refreshCookie);
        return new SignInResponse(signInToken.getAccessToken());
    }

    @PostMapping("/token-refresh")
    public SignInResponse refreshAccessToken(@CookieValue("refresh") Cookie refreshCookie) {
        String refreshToken = refreshCookie.getValue();
        SignInToken signInToken = signInService.refreshAccessToken(refreshToken);
        return new SignInResponse(signInToken.getAccessToken());
    }

}
