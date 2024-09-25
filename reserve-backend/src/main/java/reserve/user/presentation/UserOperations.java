package reserve.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reserve.auth.domain.AuthInfo;
import reserve.global.exception.ErrorCode;
import reserve.global.swagger.annotation.ApiErrorCodeResponse;
import reserve.global.swagger.annotation.ApiErrorCodeResponses;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.dto.response.UserInfoResponse;

@Tag(name = "Users", description = "User API")
public interface UserOperations {

    @Operation(
            summary = "Get user information",
            description = "Get user information by username",
            operationId = "1_getUserInfo"
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Response with user information",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoResponse.class)
            )
    ))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "404", errorCode = ErrorCode.USER_NOT_FOUND))
    @SuppressWarnings("unused")
    UserInfoResponse getUserInfo(@Schema(description = "Username", example = "username") String username);


    @Operation(
            summary = "Update user information",
            description = "Update the signed-in user's information",
            operationId = "2_updateUserInfo"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @ApiErrorCodeResponses(@ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO))
    @SuppressWarnings("unused")
    void updateUserInfo(AuthInfo authInfo, UserUpdateRequest userUpdateRequest);


    @Operation(
            summary = "Update user password",
            description = "Update the signed-in user's password",
            operationId = "3_updatePassword"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @ApiErrorCodeResponses({
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO),
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.WRONG_PASSWORD)
    })
    @SuppressWarnings("unused")
    void updatePassword(AuthInfo authInfo, PasswordUpdateRequest passwordUpdateRequest);


    @Operation(
            summary = "Delete user",
            description = "Delete the signed-in user",
            operationId = "4_deleteUser"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully deleted"))
    @ApiErrorCodeResponses({
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.INVALID_SIGN_IN_INFO),
            @ApiErrorCodeResponse(responseCode = "403", errorCode = ErrorCode.WRONG_PASSWORD)
    })
    @SuppressWarnings("unused")
    void delete(AuthInfo authInfo, UserDeleteRequest userDeleteRequest);

}
