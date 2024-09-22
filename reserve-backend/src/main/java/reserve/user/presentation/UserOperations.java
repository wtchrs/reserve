package reserve.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reserve.auth.domain.AuthInfo;
import reserve.global.exception.dto.ErrorResponse;
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Response with user information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            )
    })
    @SuppressWarnings("unused")
    UserInfoResponse getUserInfo(@Schema(description = "Username", example = "username") String username);

    @Operation(
            summary = "Update user information",
            description = "Update the signed-in user's information",
            operationId = "2_updateUserInfo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated"),
            @ApiResponse(
                    responseCode = "403", description = "Invalid sign-in information",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @SuppressWarnings("unused")
    void updateUserInfo(AuthInfo authInfo, UserUpdateRequest userUpdateRequest);

    @Operation(
            summary = "Update user password",
            description = "Update the signed-in user's password",
            operationId = "3_updatePassword"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully updated"))
    @SuppressWarnings("unused")
    void updatePassword(AuthInfo authInfo, PasswordUpdateRequest passwordUpdateRequest);

    @Operation(
            summary = "Delete user",
            description = "Delete the signed-in user",
            operationId = "4_deleteUser"
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully deleted"))
    @SuppressWarnings("unused")
    void delete(AuthInfo authInfo, UserDeleteRequest userDeleteRequest);

}
