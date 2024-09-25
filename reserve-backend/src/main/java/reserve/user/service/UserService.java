package reserve.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.ResourceNotFoundException;
import reserve.signup.infrastructure.PasswordEncoder;
import reserve.user.domain.User;
import reserve.user.infrastructure.UserRepository;
import reserve.user.dto.request.PasswordUpdateRequest;
import reserve.user.dto.request.UserDeleteRequest;
import reserve.user.dto.request.UserUpdateRequest;
import reserve.user.dto.response.UserInfoResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void update(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO));
        if (StringUtils.hasText(userUpdateRequest.getNickname())) {
            user.setNickname(userUpdateRequest.getNickname());
        }
        if (StringUtils.hasText(userUpdateRequest.getDescription())) {
            user.setDescription(userUpdateRequest.getDescription());
        }
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest passwordUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO));
        if (!passwordEncoder.matches(passwordUpdateRequest.getOldPassword(), user.getPasswordHash())) {
            throw new AuthenticationException(ErrorCode.WRONG_PASSWORD);
        }
        String newPassword = passwordUpdateRequest.getNewPassword();
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.changePassword(newPasswordHash);
    }

    @Transactional
    public void delete(Long userId, UserDeleteRequest userDeleteRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_SIGN_IN_INFO));
        if (!passwordEncoder.matches(userDeleteRequest.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException(ErrorCode.WRONG_PASSWORD);
        }
        userRepository.deleteById(userId);
    }

}
