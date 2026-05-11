package sk.mvp.user_service.auth.jwt;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import sk.mvp.user_service.auth.dto.LogoutReq;
import sk.mvp.user_service.auth.dto.QUserDetail;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {
    private JwtConfig jwtConfig;

    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateAccessToken(String username, int tokenVersion, String jti, String[] roles) {
        return Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .setIssuedAt(new Date())
                .claim("type", JwtTokenType.ACCESS_TOKEN.getValue())
                .claim("version", tokenVersion)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration().toMillis()))
                .signWith(jwtConfig.getAccesKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String jti) {
        return Jwts.builder()
                .setId(jti)
                .claim("type", JwtTokenType.REFRESH_TOKEN.getValue())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpiration().toMillis()))
                .signWith(jwtConfig.getRefreshKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validiation
    public void validateAccessToken(Claims claims, int expectedTokenVersion) {
        assertType(claims, JwtTokenType.ACCESS_TOKEN.getValue());
        assertTokenVersion(claims, expectedTokenVersion);
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseClaimsFromJwtToken(token, jwtConfig.getRefreshKey());
        assertType(claims, JwtTokenType.REFRESH_TOKEN.getValue());
    }

    private void assertType(Claims claims, String expected) {
        String type = claims.get("type", String.class);
        if (!expected.equals(type)) {
            throw new QApplicationException("Token Invalid", ErrorType.TOKEN_INVALID, null);
        }
    }

    private void assertTokenVersion(Claims claims, int expectedVersion) {
        Integer tokenVersion = claims.get("version", Integer.class);
        if (tokenVersion == null || tokenVersion != expectedVersion) {
            throw new QApplicationException("Token version mismatch", ErrorType.TOKEN_INVALID, null);
        }
    }

    // helper methods
    public String getUsernameFromAccessToken(String token) {
        return parseAccessToken(token).getSubject();
    }

    public Claims parseAccessToken(String token) {
        return parseClaimsFromJwtToken(token, jwtConfig.getAccesKey());
    }
    public Claims parseClaimsFromRefreshToken(String token) {
        return parseClaimsFromJwtToken(token, jwtConfig.getRefreshKey());
    }

    public Claims parseClaimsFromJwtToken(String token, SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Duration ttlUntilExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        long ttlMillis = expiration.getTime() - System.currentTimeMillis();
        return ttlMillis <= 0 ? Duration.ZERO : Duration.ofMillis(ttlMillis);
    }

    public QUserDetail getUserDetailFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        return new QUserDetail(
                claims.getSubject(),
                claims.get("roles", List.class));
    }

    public String extractAccessToken(String authHeader, String cookieAccess) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return cookieAccess;
    }

    public String extractRefreshToken(LogoutReq dto, String cookieRefresh) {
        if (dto != null && dto.refreshToken() != null) {
            return dto.refreshToken();
        }
        return cookieRefresh;
    }
    //wrapped
    public Duration getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }

    public Duration getRefreshTokenExpiration() {
        return jwtConfig.getRefreshTokenExpiration();
    }
}