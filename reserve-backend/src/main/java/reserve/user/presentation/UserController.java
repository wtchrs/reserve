package reserve.user.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reserve.auth.domain.AuthInfo;
import reserve.auth.infrastructure.Authentication;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.dto.response.UserInfoResponse;
import reserve.user.service.UserService;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController implements UserOperations {

    private final UserService userService;

    @Override
    @GetMapping("/{username}")
    public UserInfoResponse getUserInfo(@PathVariable("username") String username) {
        return userService.getUserInfo(username);
    }

    @Override
    @PutMapping
    public void updateUserInfo(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated UserUpdateRequest userUpdateRequest
    ) {
        userService.update(authInfo.getUserId(), userUpdateRequest);
    }

    @Override
    @PutMapping("/password")
    public void updatePassword(
            @Authentication AuthInfo authInfo,
            @RequestBody @Validated PasswordUpdateRequest passwordUpdateRequest
    ) {
        userService.updatePassword(authInfo.getUserId(), passwordUpdateRequest);
    }

    @Override
    @DeleteMapping
    public void delete(@Authentication AuthInfo authInfo, @RequestBody @Validated UserDeleteRequest userDeleteRequest) {
        userService.delete(authInfo.getUserId(), userDeleteRequest);
    }

}
