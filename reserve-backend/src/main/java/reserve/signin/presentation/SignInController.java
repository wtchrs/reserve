package reserve.signin.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.signin.dto.SignInToken;
import reserve.signin.dto.request.SignInRequest;
import reserve.signin.service.SignInService;

@RestController
@RequestMapping("/v1")
@Tag(name = "Sign In", description = "Sign in API")
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
    @Operation(
            summary = "Sign in",
            description = "Sign in",
            operationId = "1_signIn"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Successfully signed in",
            headers = {
                    @Header(name = "Authorization", description = "Bearer access token"),
                    @Header(name = "Set-Cookie", description = "Refresh token cookie with name 'refresh'")
            }
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "401", errorCode = ErrorCode.WRONG_CREDENTIAL))
    public void signIn(@RequestBody @Validated SignInRequest signInRequest, HttpServletResponse response) {
        SignInToken signInToken = signInService.signIn(signInRequest);
        response.setHeader("Authorization", signInToken.getAccessToken());
        response.addCookie(createRefreshCookie(signInToken));
    }

    @PostMapping("/token-refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Refresh access token",
            operationId = "2_refreshAccessToken"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Successfully refreshed",
            headers = {
                    @Header(name = "Authorization", description = "Bearer access token"),
                    @Header(name = "Set-Cookie", description = "New refresh token cookie with name 'refresh'")
            }
    ))
    @ApiErrorCodeResponses({
            @ApiErrorCodeResponse(responseCode = "401", errorCode = ErrorCode.EXPIRED_REFRESH_TOKEN),
            @ApiErrorCodeResponse(responseCode = "401", errorCode = ErrorCode.INVALID_REFRESH_TOKEN)
    })
    public void refreshAccessToken(
            @CookieValue("refresh") Cookie refreshCookie,
            HttpServletResponse response
    ) {
        String refreshTokenValue = refreshCookie.getValue();
        SignInToken signInToken = signInService.refreshAccessToken(refreshTokenValue);
        response.setHeader("Authorization", signInToken.getAccessToken());
        response.addCookie(createRefreshCookie(signInToken));
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
    @Operation(
            summary = "Sign out",
            description = "Sign out",
            operationId = "3_signOut"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully signed out"))
    public void signOut(@CookieValue("refresh") Cookie refreshCookie, HttpServletResponse response) {
        signInService.signOut(refreshCookie.getValue());
        // delete cookie
        refreshCookie.setMaxAge(0);
        refreshCookie.setValue("");
        response.addCookie(refreshCookie);
    }

}
