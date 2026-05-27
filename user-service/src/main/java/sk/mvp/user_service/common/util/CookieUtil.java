package sk.mvp.user_service.common.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import sk.mvp.user_service.auth.dto.response.TokenPair;
import sk.mvp.user_service.auth.jwt.JwtConfig;

import java.time.Duration;

@Component
public class CookieUtil {
    private JwtConfig jwtConfig;

    public CookieUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public ResponseCookie create(String key, String value, String path) {
        return ResponseCookie.from(key, value)
                .domain(jwtConfig.getCookieDomain())
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .httpOnly(jwtConfig.isCookieIsHttpOnly())
                .secure(jwtConfig.isJwtCookieIsSecure())
                .path(path)
                .sameSite(jwtConfig.getCookieIsSameSite())
                .build();
    }
    public ResponseCookie remove(String key, String path) {
        return ResponseCookie.from(key, "")
                .maxAge(Duration.ZERO)
                .domain(jwtConfig.getCookieDomain())
                .path(path)
                .httpOnly(true)
                .build();
    }
    public void setTokenCookies(HttpServletResponse response, TokenPair tokenPair) {
        ResponseCookie refreshCookie = this.create(jwtConfig.getRefreshTokenCookieName(), tokenPair.getRefreshToken(), jwtConfig.getRefreshTokenCookiePath());
        ResponseCookie accessCookie = this.create(jwtConfig.getAccessTokenCookieName(), tokenPair.getAccessToken(), "/");

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }
    public void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie resCookie = this.remove(jwtConfig.getRefreshTokenCookieName(), jwtConfig.getRefreshTokenCookiePath());
        ResponseCookie accCookie = this.remove(jwtConfig.getAccessTokenCookieName(), "/");
        response.addHeader(HttpHeaders.SET_COOKIE, resCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, accCookie.toString());
    }


}
