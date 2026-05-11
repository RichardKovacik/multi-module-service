package sk.mvp.user_service.infra.security.handler;

//@Component
//public class LoginSuccesHandler implements AuthenticationSuccessHandler {
//    private IRedisService redisService;
//    private ITokenService tokenService;
//    private JwtConfig jwtConfig;
//
//    public LoginSuccesHandler(IRedisService redisService, ITokenService tokenService, JwtConfig jwtConfig) {
//        this.redisService = redisService;
//        this.tokenService = tokenService;
//        this.jwtConfig = jwtConfig;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        QUserDetail QUserDetail = (QUserDetail) authentication.getPrincipal();
//        String loginAttemptsKey = AuthConts.REDISS_AUTH_LOGIN_ATTEMPTS_USER_COLL + QUserDetail.getUsername();
//        // delete attempts counter in reddis
//        redisService.delete(loginAttemptsKey);
//        //generate token pair
//        TokenPair tokenPair = tokenService.generateTokenPair(QUserDetail);
//        // send token to cookie
//        //place refresh token in httopnly cookie
//        ResponseCookie refreshCookie = CookieUtil.create("refresh_token",
//                tokenPair.getRefreshToken(),
//                jwtConfig.getCookieDomain(),
//                Duration.ofMillis(jwtConfig.getRefreshTokenExpirationInMls()),
//                jwtConfig.isCookieIsHttpOnly(),
//                jwtConfig.isJwtCookieIsSecure(),
//                jwtConfig.getRefreshTokenCookiePath(),
//                jwtConfig.getCookieIsSameSite()
//        );
//        //place access token in http-only cookie
//        ResponseCookie accessCookie = CookieUtil.create("access_token",
//                tokenPair.getAccessToken(),
//                jwtConfig.getCookieDomain(),
//                Duration.ofMillis(jwtConfig.getAccessTokenExpirationInMls()),
//                jwtConfig.isCookieIsHttpOnly(),
//                jwtConfig.isJwtCookieIsSecure(),
//                "/",
//                jwtConfig.getCookieIsSameSite()
//        );
//        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
//        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
//        response.setStatus(HttpServletResponse.SC_OK);
//
//
//    }
//}
