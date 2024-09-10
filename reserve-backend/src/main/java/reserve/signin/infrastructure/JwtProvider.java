package reserve.signin.infrastructure;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reserve.global.exception.AccessTokenException;
import reserve.global.exception.ErrorCode;
import reserve.global.exception.InvalidAuthorizationException;
import reserve.signin.domain.TokenDetails;
import reserve.signin.dto.SignInToken;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

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

    public SignInToken generateSignInToken(TokenDetails tokenDetails) {
        Map<String, String> claims =
                Map.of("username", tokenDetails.getUsername(), "nickname", tokenDetails.getNickname());
        String accessToken = generateAccessToken(tokenDetails.getUserId(), claims);
        String refreshToken = generateRefreshToken(tokenDetails.getUserId(), Map.of());
        return new SignInToken(accessToken, refreshToken);
    }

    private String generateAccessToken(String subject, Map<String, String> claims) {
        return generateToken(subject, claims, accessTokenExpPeriod, accessTokenSigningKey);
    }

    private String generateRefreshToken(String subject, Map<String, String> claims) {
        return generateToken(subject, claims, refreshTokenExpPeriod, refreshTokenSigningKey);
    }

    private String generateToken(String subject, Map<String, String> claims, int expirationPeriod, Key signingKey) {
        Instant now = Instant.now();
        Date issued = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(expirationPeriod * 1000L));
        return Jwts.builder()
                .setHeaderParam(JWT_TYPE_HEADER_NAME, JWT_TYPE_HEADER_VALUE)
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(expiration)
                .setIssuedAt(issued)
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

    public TokenDetails extractAccessTokenDetails(String jwt) {
        try {
            Claims body = parseToken(jwt, accessTokenSigningKey).getBody();
            return new TokenDetails(
                    body.getSubject(),
                    body.get("username", String.class),
                    body.get("nickname", String.class)
            );
        } catch (ExpiredJwtException e) {
            throw new AccessTokenException(ErrorCode.EXPIRED_ACCESS_TOKEN, e);
        } catch (JwtException e) {
            throw new InvalidAuthorizationException(ErrorCode.INVALID_ACCESS_TOKEN_FORMAT, e);
        }
    }

    private static Jws<Claims> parseToken(String jwt, Key signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(jwt);
    }

}
