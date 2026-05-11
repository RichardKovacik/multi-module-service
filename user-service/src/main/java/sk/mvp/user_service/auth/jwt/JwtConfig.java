package sk.mvp.user_service.auth.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@Getter
//cofig file acces and refesh toekn
public class JwtConfig {
    @Value("${jwt.cookie.isSecure}")
    private boolean jwtCookieIsSecure;
    @Value("${jwt.cookie.isHttpOnly}")
    private boolean cookieIsHttpOnly;
    @Value("${jwt.cookie.sameSite}")
    private String cookieIsSameSite;
    @Value("${jwt.cookie.domain}")
    private String cookieDomain;
    //access
    @Value("${jwt.access-token.expiration:PT15M}")
    private Duration accessTokenExpiration;
    @Value("${jwt.access-token.cookie-name}")
    private String accessTokenCookieName;
    @Value("${jwt.access-token.secret}")
    private String accessTokenSecret;
    //refresh
    @Value("${jwt.refresh-token.expiration:P7D}")
    private Duration refreshTokenExpiration;
    @Value("${jwt.refresh-token.secret}")
    private String refreshTokenSecret;
    @Value("${jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;
    @Value("${jwt.refresh-token.cookie.path}")
    private String refreshTokenCookiePath;

    private SecretKey accesKey;
    private SecretKey refreshKey;

    // Initializes the key after the class is instantiated
    @PostConstruct
    private void init() {
        this.accesKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    }

}
