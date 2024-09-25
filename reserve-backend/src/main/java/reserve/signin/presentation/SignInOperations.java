package reserve.signin.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.signin.dto.request.SignInRequest;

@Tag(name = "Sign In", description = "Sign in API")
public interface SignInOperations {

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
    @SuppressWarnings("unused")
    void signIn(SignInRequest signInRequest, HttpServletResponse response);


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
    @SuppressWarnings("unused")
    void refreshAccessToken(Cookie refreshCookie, HttpServletResponse response);


    @Operation(
            summary = "Sign out",
            description = "Sign out",
            operationId = "3_signOut"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully signed out"))
    @SuppressWarnings("unused")
    void signOut(Cookie refreshCookie, HttpServletResponse response);

}
