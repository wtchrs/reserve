package reserve.signin.infrastructure;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.dto.SignInToken;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    public static final String EMPTY_SUBJECT_PLACEHOLDER = "";

    public static final String JWT_TYPE_HEADER_NAME = "typ";
    public static final String JWT_TYPE_HEADER_VALUE = "JWT";
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final Key secretKey;
    private final int accessTokenExpPeriod;
    private final int refreshTokenExpPeriod;

    public JwtProvider(
            @Value("${application.security.jwt.secretKey}") String secretKeyString,
            @Value("${application.security.jwt.accessTokenExpire}") int accessTokenExpPeriod,
            @Value("${application.security.jwt.refreshTokenExpire}") int refreshTokenExpPeriod
    ) {
        this.secretKey =
                new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM.getJcaName());
        this.accessTokenExpPeriod = accessTokenExpPeriod;
        this.refreshTokenExpPeriod = refreshTokenExpPeriod;
    }

    public SignInToken generateSignInToken(String subject) {
        String accessToken = generateToken(subject, accessTokenExpPeriod);
        String refreshToken = generateToken(EMPTY_SUBJECT_PLACEHOLDER, refreshTokenExpPeriod);
        return new SignInToken(accessToken, refreshToken);
    }

    private String generateToken(String subject, int expirationPeriod) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationPeriod);
        return Jwts.builder()
                .setHeaderParam(JWT_TYPE_HEADER_NAME, JWT_TYPE_HEADER_VALUE)
                .setSubject(subject)
                .setExpiration(expiration)
                .setIssuedAt(now)
                .signWith(secretKey)
                .compact();
    }

    public boolean isAccessTokenExpired(String jwt) {
        try {
            parseToken(jwt);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_ACCESS_TOKEN_FORMAT);
        }
        return false;
    }

    public boolean isRefreshTokenExpired(String jwt) {
        try {
            parseToken(jwt);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_REFRESH_TOKEN_FORMAT);
        }
        return false;
    }

    public String extractSubject(String jwt) {
        return parseToken(jwt).getBody().getSubject();
    }

    private Jws<Claims> parseToken(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt);
    }

}
