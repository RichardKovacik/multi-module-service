package sk.mvp.user_service.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.mvp.user_service.auth.jwt.JwtProvider;
import sk.mvp.user_service.common.util.CookieUtil;
import sk.mvp.user_service.auth.dto.*;
import sk.mvp.user_service.auth.service.IAuthService;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.user.dto.UserProfile;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private IAuthService authService;
    private CookieUtil cookieUtil;
    private JwtProvider jwtProvider;

    public AuthController(IAuthService authService,
                          CookieUtil cookieUtil,
                          JwtProvider jwtProvider) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginReq loginReq,
                                   @RequestHeader(value = "X-Client-Type", defaultValue = "web") String clientType,
                                   HttpServletResponse servletResponse) {

        TokenPair tokenPair = authService.loginUser(loginReq);

        // mobile login
        if (clientType.equals("mobile")){
            return ResponseEntity.ok(tokenPair);
        }
        // web cookie login
        cookieUtil.setTokenCookies(servletResponse, tokenPair);
        return ResponseEntity.ok().build();

    }

    /**
     * Refresh token from cookie or json
     * @return
     */
    @PostMapping(value = "/refresh/tokens")
    public ResponseEntity<?> refreshTokens(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                              @RequestHeader(value = "X-Client-Type", defaultValue = "web", required = false) String clientType,
                                              @RequestBody(required = false) @Valid RefreshTokenReq refreshRequest, // Pre mobil
                                              HttpServletResponse servletResponse) {
        String tokenToUse = (refreshToken != null) ? refreshToken :
                (refreshRequest != null ? refreshRequest.getToken() : null);

        if (tokenToUse == null || tokenToUse.isBlank()) {
            throw new QApplicationException("Missing refresh token", ErrorType.AUTH_BAD_REQUEST, null);
        }

        TokenPair tokenPair = authService.refreshTokens(tokenToUse);

        // mobile
        if (clientType.equalsIgnoreCase("mobile")){
            return ResponseEntity.ok(tokenPair);
        }
        // web cookie login
        cookieUtil.setTokenCookies(servletResponse, tokenPair);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/register")
    public UserProfile createUser(@RequestBody @Valid RegistrationReq registrationReq) {
        return authService.registerUser(registrationReq);
    }

    @PostMapping(value = "/email/confirm")
    public VerificationTokenResponse verifyToken(@RequestParam("token") @NotNull @NotBlank String token) {
        return authService.verifyEmailVerificationToken(token);
    }


    @PostMapping(value = "/logout")
    public ResponseEntity<?> logoutUser(@CookieValue(name = "refresh_token", required = false) String cookieRefresh,
                                        @CookieValue(name = "access_token", required = false) String cookieAccess,
                                        @RequestHeader(value = "X-Client-Type", defaultValue = "web") String clientType,
                                        @RequestBody(required = false) @Valid LogoutReq logoutReq,
                                        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                        HttpServletResponse response) {

        String access = jwtProvider.extractAccessToken(authHeader, cookieAccess);
        String refresh = jwtProvider.extractRefreshToken(logoutReq, cookieRefresh);

        if (refresh == null) {
            throw new QApplicationException("Missing refresh token", ErrorType.AUTH_BAD_REQUEST, null);
        }

        //invalidate tokens in reddis
        authService.logout(refresh, access);
        // clear tokens in cookie only for web
        if ("web".equalsIgnoreCase(clientType)) {
            cookieUtil.clearTokenCookies(response);
        }


        return ResponseEntity.ok().build();
    }
}
