package sk.mvp.user_service.unit.auth;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import sk.mvp.user_service.infra.security.QUserDetail;
import sk.mvp.user_service.auth.dto.TokenPair;
import sk.mvp.user_service.auth.handler.LoginSuccesHandler;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.auth.jwt.JwtConfig;
import sk.mvp.user_service.infra.reddis.IRedisService;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LoginSuccesHandlerTest {
    @Mock
    private IRedisService redisService;
    @Mock
    private ITokenService tokenService;
    @Mock
    private JwtConfig jwtConfig;

    private LoginSuccesHandler successHandler; // Your actual class name here

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;


    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Initialize the mock with your specific config values
        setupJwtConfigMock();

        successHandler = new LoginSuccesHandler(redisService, tokenService, jwtConfig);
    }


    @Test
    public void onAuthenticationSuccess_ShouldReturnTokenPairInCookie_WhenDataIsValid() throws ServletException, IOException {
        // 1. Prepare Data using factory methods
        QUserDetail userDetail = createUserDetails("testUser");
        Authentication auth = createAuthentication(userDetail);
        TokenPair mockTokens = createTokenPair("access-123", "refresh-456");

        // 2. Setup Mock behavior
        when(tokenService.generateTokenPair(userDetail)).thenReturn(mockTokens);

        //act
        successHandler.onAuthenticationSuccess(request, response, auth);
        //assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(redisService).delete(anyString());

        //check cookie
        List<String> cookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeaders, "Cookie headers should not be null");

        // acces token verification
        String accessCookie = cookieHeaders.stream()
                .filter(c -> c.contains("access_token="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Access token cookie missing"));
        assertTrue(accessCookie.contains("access-123"), "Access cookie should contain the correct token value");

        // 4. Detailed verification for Refresh Token
        String refreshCookie = cookieHeaders.stream()
                .filter(c -> c.contains("refresh_token="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Refresh token cookie missing"));

        assertTrue(refreshCookie.contains("refresh-456"), "Refresh cookie should contain the correct token value");

    }

    private void setupJwtConfigMock() {
        // Stubbing all getters based on your property file values
        lenient().when(jwtConfig.getSecret()).thenReturn("very-long-secret-key-at-least-32-chars-long");
        lenient().when(jwtConfig.getAccessTokenExpirationInMls()).thenReturn(3600000L);
        lenient().when(jwtConfig.getRefreshTokenExpirationInMls()).thenReturn(86400000L);
        lenient().when(jwtConfig.getCookieDomain()).thenReturn("localhost");
        lenient().when(jwtConfig.isJwtCookieIsSecure()).thenReturn(true);
        lenient().when(jwtConfig.isCookieIsHttpOnly()).thenReturn(true);
        lenient().when(jwtConfig.getCookieIsSameSite()).thenReturn("Lax");
        lenient().when(jwtConfig.getRefreshTokenCookiePath()).thenReturn("api/auth/web/refresh/tokens");

        // Stubbing the Keys (since @PostConstruct won't run on a mock)
        SecretKey mockKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        lenient().when(jwtConfig.getAccesKey()).thenReturn(mockKey);
        lenient().when(jwtConfig.getRefreshKey()).thenReturn(mockKey);
    }



    private QUserDetail createUserDetails(String username) {
        return new QUserDetail(
                username,
                Collections.emptyList()
        );
    }

    private Authentication createAuthentication(QUserDetail principal) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    private TokenPair createTokenPair(String accessToken, String refreshToken) {
        return new TokenPair(refreshToken, accessToken);
    }
}
