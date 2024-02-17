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

    public static final String JWT_TYPE_HEADER_NAME = "typ";
    public static final String JWT_TYPE_HEADER_VALUE = "JWT";
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final Key accessTokenSigningKey;
    private final Key refreshTokenSigningKey;
    private final int accessTokenExpPeriod;
    private final int refreshTokenExpPeriod;

    public JwtProvider(
            @Value("${application.security.jwt.accessTokenSecretKey}") String accessTokenSecret,
            @Value("${application.security.jwt.refreshTokenSecretKey}") String refreshTokenSecret,
            @Value("${application.security.jwt.accessTokenExpire}") int accessTokenExpPeriod,
            @Value("${application.security.jwt.refreshTokenExpire}") int refreshTokenExpPeriod
    ) {
        this.accessTokenSigningKey = new SecretKeySpec(
                accessTokenSecret.getBytes(StandardCharsets.UTF_8),
                SIGNATURE_ALGORITHM.getJcaName()
        );
        this.refreshTokenSigningKey = new SecretKeySpec(
                refreshTokenSecret.getBytes(StandardCharsets.UTF_8),
                SIGNATURE_ALGORITHM.getJcaName()
        );
        this.accessTokenExpPeriod = accessTokenExpPeriod;
        this.refreshTokenExpPeriod = refreshTokenExpPeriod;
    }

    public SignInToken generateSignInToken(String subject) {
        String accessToken = generateAccessToken(subject);
        String refreshToken = generateRefreshToken(subject);
        return new SignInToken(accessToken, refreshToken);
    }

    private String generateAccessToken(String subject) {
        return generateToken(subject, accessTokenExpPeriod, accessTokenSigningKey);
    }

    private String generateRefreshToken(String subject) {
        return generateToken(subject, refreshTokenExpPeriod, refreshTokenSigningKey);
    }

    private String generateToken(String subject, int expirationPeriod, Key signingKey) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationPeriod * 1000L);
        return Jwts.builder()
                .setHeaderParam(JWT_TYPE_HEADER_NAME, JWT_TYPE_HEADER_VALUE)
                .setSubject(subject)
                .setExpiration(expiration)
                .setIssuedAt(now)
                .signWith(signingKey)
                .compact();
    }

    public boolean isAccessTokenExpired(String jwt) {
        try {
            parseToken(jwt, accessTokenSigningKey);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_ACCESS_TOKEN_FORMAT, e);
        }
        return false;
    }

    public boolean isRefreshTokenExpired(String jwt) {
        try {
            parseToken(jwt, refreshTokenSigningKey);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_REFRESH_TOKEN_FORMAT, e);
        }
        return false;
    }

    public String extractAccessTokenSubject(String jwt) {
        return extractSubject(jwt, accessTokenSigningKey);
    }

    public String extractRefreshTokenSubject(String jwt) {
        return extractSubject(jwt, refreshTokenSigningKey);
    }

    private String extractSubject(String jwt, Key signingKey) {
        return parseToken(jwt, signingKey).getBody().getSubject();
    }

    private static Jws<Claims> parseToken(String jwt, Key signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(jwt);
    }

}
