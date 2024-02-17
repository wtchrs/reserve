package reserve.auth.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import reserve.auth.domain.AuthInfo;
import reserve.global.exception.AccessTokenException;
import reserve.global.exception.AuthenticationException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.infrastructure.JwtProvider;

@Component
@RequiredArgsConstructor
public class AuthInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthInfo.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authorization = request.getHeader("Authorization");
        AuthInfo authInfo = getAuthInfo(authorization);
        return checkAuthentication(parameter, authInfo);
    }

    private AuthInfo getAuthInfo(String authorizationHeader) {
        if (authorizationHeader == null) {
            return AuthInfo.guest();
        }
        String token = extractAccessToken(authorizationHeader);
        if (token == null) {
            return AuthInfo.guest(true);
        }
        Long userId = Long.valueOf(jwtProvider.extractAccessTokenSubject(token));
        return AuthInfo.user(userId);
    }

    private String extractAccessToken(String authorization) {
        String[] authParts = authorization.split(" ");
        if (authParts.length != 2 || !authParts[0].equalsIgnoreCase("Bearer")) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_ACCESS_TOKEN_FORMAT);
        }
        if (jwtProvider.isAccessTokenExpired(authParts[1])) {
            return null;
        }
        return authParts[1];
    }

    private AuthInfo checkAuthentication(MethodParameter parameter, AuthInfo authInfo) {
        Authentication authentication = parameter.getParameterAnnotation(Authentication.class);
        if (authentication != null && authentication.required() && authInfo.isGuest()) {
            if (authInfo.isExpired()) {
                throw new AccessTokenException(ErrorCode.EXPIRED_ACCESS_TOKEN);
            }
            throw new AuthenticationException(ErrorCode.SIGN_IN_REQUIRED);
        }
        return authInfo;
    }

}
